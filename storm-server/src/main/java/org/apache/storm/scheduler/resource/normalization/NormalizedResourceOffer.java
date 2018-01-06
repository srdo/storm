/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.storm.scheduler.resource.normalization;

import java.util.Map;
import org.apache.storm.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An offer of resources with normalized resource names.
 */
public class NormalizedResourceOffer implements NormalizedResourcesWithMemory {

    private static final Logger LOG = LoggerFactory.getLogger(NormalizedResourceOffer.class);
    private final NormalizedResources normalizedResources;
    private double totalMemoryMb;

    /**
     * Create a new normalized resource offer.
     *
     * @param resources the resources to be normalized.
     */
    public NormalizedResourceOffer(Map<String, ? extends Number> resources) {
        Map<String, Double> normalizedResourceMap = NormalizedResources.RESOURCE_NAME_NORMALIZER.normalizedResourceMap(resources);
        totalMemoryMb = normalizedResourceMap.getOrDefault(Constants.COMMON_TOTAL_MEMORY_RESOURCE_NAME, 0.0);
        this.normalizedResources = new NormalizedResources(normalizedResourceMap);
    }

    public NormalizedResourceOffer() {
        this((Map<String, ? extends Number>) null);
    }

    public NormalizedResourceOffer(NormalizedResourceOffer other) {
        this.totalMemoryMb = other.totalMemoryMb;
        this.normalizedResources = new NormalizedResources(other.normalizedResources);
    }

    @Override
    public double getTotalMemoryMb() {
        return totalMemoryMb;
    }

    public Map<String, Double> toNormalizedMap() {
        Map<String, Double> ret = normalizedResources.toNormalizedMap();
        ret.put(Constants.COMMON_TOTAL_MEMORY_RESOURCE_NAME, totalMemoryMb);
        return ret;
    }

    public void add(NormalizedResourcesWithMemory other) {
        normalizedResources.add(other.getNormalizedResources());
        totalMemoryMb += other.getTotalMemoryMb();
    }

    public void remove(NormalizedResourcesWithMemory other) {
        normalizedResources.remove(other.getNormalizedResources());
        totalMemoryMb -= other.getTotalMemoryMb();
        assert totalMemoryMb >= 0.0;
    }

    /**
     * Calculate the average resource usage percentage with this being the total resources and used being the amounts used.
     *
     * @param used the amount of resources used.
     * @return the average percentage used 0.0 to 100.0. Clamps to 100.0 in case there are no available resources in the total
     */
    public double calculateAveragePercentageUsedBy(NormalizedResourcesWithMemory used) {
        return normalizedResources.calculateAveragePercentageUsedBy(
            used.getNormalizedResources(), getTotalMemoryMb(), used.getTotalMemoryMb());
    }

    /**
     * Calculate the minimum resource usage percentage with this being the total resources and used being the amounts used.
     *
     * @param used the amount of resources used.
     * @return the minimum percentage used 0.0 to 100.0. Clamps to 100.0 in case there are no available resources in the total.
     */
    public double calculateMinPercentageUsedBy(NormalizedResourcesWithMemory used) {
        return normalizedResources.calculateMinPercentageUsedBy(used.getNormalizedResources(), getTotalMemoryMb(), used.getTotalMemoryMb());
    }

    /**
     * A simple sanity check to see if all of the resources in this would be large enough to hold the resources in other ignoring memory. It
     * does not check memory because with shared memory it is beyond the scope of this.
     *
     * @param other the resources that we want to check if they would fit in this.
     * @return true if it might fit, else false if it could not possibly fit.
     */
    public boolean couldHoldIgnoringSharedMemory(NormalizedResourcesWithMemory other) {
        return normalizedResources.couldHoldIgnoringSharedMemory(
            other.getNormalizedResources(), getTotalMemoryMb(), other.getTotalMemoryMb());
    }

    public double getTotalCpu() {
        return normalizedResources.getTotalCpu();
    }

    @Override
    public NormalizedResources getNormalizedResources() {
        return normalizedResources;
    }
}

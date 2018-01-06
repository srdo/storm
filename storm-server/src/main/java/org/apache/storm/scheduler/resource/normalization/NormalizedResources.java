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

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.lang.Validate;
import org.apache.storm.Constants;
import org.apache.storm.generated.WorkerResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resources that have been normalized. This class is intended as a delegate for more specific types of normalized resource set, since it
 * does not keep track of memory as a resource.
 */
public class NormalizedResources {

    private static final Logger LOG = LoggerFactory.getLogger(NormalizedResources.class);

    public static ResourceNameNormalizer RESOURCE_NAME_NORMALIZER;
    private static ResourceMapArrayBridge RESOURCE_ARRAY_BRIDGE;

    static {
        resetResourceNames();
    }

    private double cpu;
    private double[] otherResources;

    /**
     * This is for testing only. It allows a test to reset the static state relating to resource names. We reset the mapping because some
     * algorithms sadly have different behavior if a resource exists or not.
     */
    @VisibleForTesting
    public static void resetResourceNames() {
        RESOURCE_NAME_NORMALIZER = new ResourceNameNormalizer();
        RESOURCE_ARRAY_BRIDGE = new ResourceMapArrayBridge();
    }

    /**
     * Copy constructor.
     */
    public NormalizedResources(NormalizedResources other) {
        cpu = other.cpu;
        otherResources = Arrays.copyOf(other.otherResources, other.otherResources.length);
    }

    /**
     * Create a new normalized set of resources. Note that memory is not managed by this class, as it is not consistent in requests vs
     * offers because of how on heap vs off heap is used.
     *
     * @param normalizedResources the normalized resource map
     * @param getTotalMemoryMb Supplier of total memory in MB.
     */
    public NormalizedResources(Map<String, Double> normalizedResources) {
        cpu = normalizedResources.getOrDefault(Constants.COMMON_CPU_RESOURCE_NAME, 0.0);
        otherResources = RESOURCE_ARRAY_BRIDGE.translateToResourceArray(normalizedResources);
    }

    /**
     * Get the total amount of cpu.
     *
     * @return the amount of cpu.
     */
    public double getTotalCpu() {
        return cpu;
    }

    private void add(double[] resourceArray) {
        int otherLength = resourceArray.length;
        int length = otherResources.length;
        if (otherLength > length) {
            double[] newResources = new double[otherLength];
            System.arraycopy(otherResources, 0, newResources, 0, length);
            otherResources = newResources;
        }
        for (int i = 0; i < otherLength; i++) {
            otherResources[i] += resourceArray[i];
        }
    }

    public void add(NormalizedResources other) {
        this.cpu += other.cpu;
        add(other.otherResources);
    }

    /**
     * Add the resources from a worker to this.
     *
     * @param value the worker resources that should be added to this.
     */
    public void add(WorkerResources value) {
        Map<String, Double> workerNormalizedResources = value.get_resources();
        cpu += workerNormalizedResources.getOrDefault(Constants.COMMON_CPU_RESOURCE_NAME, 0.0);
        add(RESOURCE_ARRAY_BRIDGE.translateToResourceArray(workerNormalizedResources));
    }

    /**
     * Remove the other resources from this. This is the same as subtracting the resources in other from this.
     *
     * @param other the resources we want removed.
     * @throws IllegalArgumentException if subtracting other from this would result in any resource amount becoming negative.
     */
    public void remove(NormalizedResources other) {
        this.cpu -= other.cpu;
        if (cpu < 0.0) {
            throwBecauseResourceBecameNegative(Constants.COMMON_CPU_RESOURCE_NAME, cpu, other.cpu);
        }
        int otherLength = other.otherResources.length;
        int length = otherResources.length;
        if (otherLength > length) {
            double[] newResources = new double[otherLength];
            System.arraycopy(otherResources, 0, newResources, 0, length);
            otherResources = newResources;
        }
        for (int i = 0; i < otherLength; i++) {
            otherResources[i] -= other.otherResources[i];
            if (otherResources[i] < 0.0) {
                throwBecauseResourceBecameNegative(getResourceNameForResourceIndex(i), otherResources[i], other.otherResources[i]);
            }
        }
    }

    @Override
    public String toString() {
        return "Normalized resources: " + toNormalizedMap();
    }

    /**
     * Return a Map of the normalized resource name to a double. This should only be used when returning thrift resource requests to the end
     * user.
     */
    public Map<String, Double> toNormalizedMap() {
        Map<String, Double> ret = RESOURCE_ARRAY_BRIDGE.translateFromResourceArray(otherResources);
        ret.put(Constants.COMMON_CPU_RESOURCE_NAME, cpu);
        return ret;
    }

    private double getResourceAt(int index) {
        if (index >= otherResources.length) {
            return 0.0;
        }
        return otherResources[index];
    }

    /**
     * A simple sanity check to see if all of the resources in this would be large enough to hold the resources in other ignoring memory. It
     * does not check memory because with shared memory it is beyond the scope of this.
     *
     * @param other the resources that we want to check if they would fit in this.
     * @param thisTotalMemoryMb The total memory in MB of this
     * @param otherTotalMemoryMb The total memory in MB of other
     * @return true if it might fit, else false if it could not possibly fit.
     */
    public boolean couldHoldIgnoringSharedMemory(NormalizedResources other, double thisTotalMemoryMb, double otherTotalMemoryMb) {
        if (this.cpu < other.getTotalCpu()) {
            return false;
        }
        int length = Math.max(this.otherResources.length, other.otherResources.length);
        for (int i = 0; i < length; i++) {
            if (getResourceAt(i) < other.getResourceAt(i)) {
                return false;
            }
        }

        return thisTotalMemoryMb >= otherTotalMemoryMb;
    }

    private String getResourceNameForResourceIndex(int resourceIndex) {
        for (Map.Entry<String, Integer> entry : RESOURCE_ARRAY_BRIDGE.getResourceNamesToArrayIndex().entrySet()) {
            int index = entry.getValue();
            if (index == resourceIndex) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void throwBecauseResourceBecameNegative(String resourceName, double currentValue, double subtractedValue) {
        throw new IllegalArgumentException(String.format("Resource amounts should never be negative."
            + " Resource '%s' with current value '%f' became negative because '%f' was removed.",
            resourceName, currentValue, subtractedValue));
    }

    private void throwBecauseResourceIsMissingFromTotal(int resourceIndex) {
        String resourceName = getResourceNameForResourceIndex(resourceIndex);
        if (resourceName == null) {
            throw new IllegalArgumentException("Array index " + resourceIndex + " is not mapped in the resource names map."
                + " This should not be possible, and is likely a bug in the Storm code.");
        }
        throw new IllegalArgumentException("Total resources does not contain resource '"
            + resourceName
            + "'. All resources should be represented in the total. This is likely a bug in the Storm code");
    }

    /**
     * Calculate the average resource usage percentage with this being the total resources and used being the amounts used. If a resource is
     * missing or zero in the total, it will be considered to be 0 and skipped in the average to avoid division by 0. If all resources are
     * skipped the result is defined to be 100.0.
     *
     * @param used the amount of resources used.
     * @param thisTotalMemoryMb The total memory in MB of this
     * @param usedTotalMemoryMb The total memory in MB of other
     * @return the average percentage used 0.0 to 100.0. Clamps to 100.0 in case there are no available resources in the total
     */
    public double calculateAveragePercentageUsedBy(NormalizedResources used, double thisTotalMemoryMb, double usedTotalMemoryMb) {
        int skippedResourceTypes = 0;
        double total = 0.0;
        if (thisTotalMemoryMb != 0.0) {
            total += usedTotalMemoryMb / thisTotalMemoryMb;
        } else {
            skippedResourceTypes++;
        }
        double totalCpu = getTotalCpu();
        if (totalCpu != 0.0) {
            total += used.getTotalCpu() / getTotalCpu();
        } else {
            skippedResourceTypes++;
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Calculating avg percentage used by. Used Mem: {} Total Mem: {}"
                + " Used Normalized Resources: {} Total Normalized Resources: {}", thisTotalMemoryMb, usedTotalMemoryMb,
                toNormalizedMap(), used.toNormalizedMap());
        }

        if (used.otherResources.length > otherResources.length) {
            throwBecauseResourceIsMissingFromTotal(used.otherResources.length);
        }

        for (int i = 0; i < otherResources.length; i++) {
            double totalValue = otherResources[i];
            if (totalValue == 0.0) {
                //Skip any resources where the total is 0, the percent used for this resource isn't meaningful.
                //We fall back to prioritizing by cpu, memory and any other resources by ignoring this value
                skippedResourceTypes++;
                continue;
            }
            double usedValue;
            if (i >= used.otherResources.length) {
                //Resources missing from used are using none of that resource
                usedValue = 0.0;
            } else {
                usedValue = used.otherResources[i];
            }
            total += usedValue / totalValue;
        }
        //Adjust the divisor for the average to account for any skipped resources (those where the total was 0)
        int divisor = 2 + otherResources.length - skippedResourceTypes;
        if (divisor == 0) {
            /*This is an arbitrary choice to make the result consistent with calculateMin.
             Any value would be valid here, becase there are no (non-zero) resources in the total set of resources,
             so we're trying to average 0 values.
             */
            return 100.0;
        } else {
            return (total * 100.0) / divisor;
        }
    }

    /**
     * Calculate the minimum resource usage percentage with this being the total resources and used being the amounts used.
     *
     * @param used the amount of resources used.
     * @param thisTotalMemoryMb The total memory in MB of this
     * @param usedTotalMemoryMb The total memory in MB of other
     * @return the minimum percentage used 0.0 to 100.0. Clamps to 100.0 in case there are no available resources in the total.
     */
    public double calculateMinPercentageUsedBy(NormalizedResources used, double thisTotalMemoryMb, double usedTotalMemoryMb) {
        double totalCpu = getTotalCpu();

        if (LOG.isTraceEnabled()) {
            LOG.trace("Calculating min percentage used by. Used Mem: {} Total Mem: {}"
                + " Used Normalized Resources: {} Total Normalized Resources: {}", thisTotalMemoryMb, usedTotalMemoryMb,
                toNormalizedMap(), used.toNormalizedMap());
        }

        if (used.otherResources.length > otherResources.length) {
            throwBecauseResourceIsMissingFromTotal(used.otherResources.length);
        }

        if (used.otherResources.length != otherResources.length
            || thisTotalMemoryMb == 0.0
            || totalCpu == 0.0) {
            //If the lengths don't match one of the resources will be 0, which means we would calculate the percentage to be 0.0
            // and so the min would be 0.0 (assuming that we can never go negative on a resource being used.
            return 0.0;
        }

        double min = 100.0;
        if (thisTotalMemoryMb != 0.0) {
            min = Math.min(min, usedTotalMemoryMb / thisTotalMemoryMb);
        }
        if (totalCpu != 0.0) {
            min = Math.min(min, used.getTotalCpu() / totalCpu);
        }

        for (int i = 0; i < otherResources.length; i++) {
            if (otherResources[i] == 0.0) {
                //Skip any resources where the total is 0, the percent used for this resource isn't meaningful.
                //We fall back to prioritizing by cpu, memory and any other resources by ignoring this value
                continue;
            }
            if (i >= used.otherResources.length) {
                //Resources missing from used are using none of that resource
                return 0;
            }
            min = Math.min(min, used.otherResources[i] / otherResources[i]);
        }
        return min * 100.0;
    }
}

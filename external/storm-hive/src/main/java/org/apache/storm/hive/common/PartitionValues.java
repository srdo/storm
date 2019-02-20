/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.apache.storm.hive.common;

import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PartitionValues {

    private final List<String> partitionValues;

    public PartitionValues(List<String> partitionValues) {
        this.partitionValues = partitionValues;
    }

    public List<String> getPartitionValues() {
        return partitionValues;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(partitionValues)
            .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        PartitionValues values = (PartitionValues) obj;
        return new EqualsBuilder()
            .append(partitionValues, values.partitionValues)
            .build();
    }

}

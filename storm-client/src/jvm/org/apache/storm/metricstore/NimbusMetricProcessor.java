/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.apache.storm.metricstore;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.storm.generated.WorkerMetricList2;
import org.apache.storm.generated.WorkerMetricPoint2;
import org.apache.storm.generated.WorkerMetrics2;
import org.apache.storm.metrics2.MetricPointForStormUi;
import org.apache.storm.thrift.TException;
import org.apache.storm.utils.NimbusClient;
import org.apache.storm.utils.NimbusLeaderNotFoundException;
import org.apache.storm.utils.Utils;

/**
 * Implementation of WorkerMetricsProcessor that sends metric data to Nimbus for processing.
 */
public class NimbusMetricProcessor implements WorkerMetricsProcessor {
    
    private String hostName;
    private Map<String, Object> conf;
    
    @Override
    public void processWorkerMetrics(List<MetricPointForStormUi> metrics,
        String topologyId, int workerPort) throws MetricException {
        
        Map<MetricListBucket, List<MetricPointForStormUi>> metricPointsByBucket = new HashMap<>();
        for (MetricPointForStormUi metricPoint : metrics) {
            MetricListBucket bucket = new MetricListBucket(metricPoint);
            metricPointsByBucket.computeIfAbsent(bucket, key -> new ArrayList<>())
                .add(metricPoint);
        }
        
        List<WorkerMetricList2> metricLists = metricPointsByBucket.entrySet().stream()
            .map(entry -> {
                List<WorkerMetricPoint2> metricsForBucket = entry.getValue().stream()
                    .map(metricPoint -> new WorkerMetricPoint2(metricPoint.getMetricName(), metricPoint.getMetricValue(),
                    metricPoint.getTimestampMs()))
                    .collect(Collectors.toList());
                return new WorkerMetricList2(metricsForBucket, entry.getKey().componentId, entry.getKey().taskId);
            })
            .collect(Collectors.toList());
        
        WorkerMetrics2 workerMetrics = new WorkerMetrics2(topologyId, workerPort, hostName, metricLists);
        
        try (NimbusClient client = NimbusClient.getConfiguredClient(conf)) {
            client.getClient().processWorkerMetrics2(workerMetrics);
        } catch (TException | NimbusLeaderNotFoundException e) {
            throw new MetricException("Failed to process metrics", e);
        }
    }

    @Override
    public void prepare(Map<String, Object> config) throws MetricException {
        this.conf = conf;
        try {    
            this.hostName = Utils.hostname();
        } catch (UnknownHostException e) {
            throw new MetricException("Failed to resolve hostname", e);
        }
    }
    
    private static class MetricListBucket {
        private final String componentId;
        private final int taskId;
        private final Map<String, String> extraContextNameToValue;

        public MetricListBucket(MetricPointForStormUi metricPoint) {
            this.componentId = metricPoint.getComponentId();
            this.taskId = metricPoint.getTaskId();
            this.extraContextNameToValue = new HashMap<>();
            metricPoint.getStreamId().ifPresent(streamId -> this.extraContextNameToValue.put("streamId", streamId));
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + Objects.hashCode(this.componentId);
            hash = 41 * hash + this.taskId;
            hash = 41 * hash + Objects.hashCode(this.extraContextNameToValue);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MetricListBucket other = (MetricListBucket) obj;
            if (this.taskId != other.taskId) {
                return false;
            }
            if (!Objects.equals(this.componentId, other.componentId)) {
                return false;
            }
            if (!Objects.equals(this.extraContextNameToValue, other.extraContextNameToValue)) {
                return false;
            }
            return true;
        }
    }
}

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

package org.apache.storm.stats;

import com.codahale.metrics.Counter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.storm.generated.ExecutorSpecificStats;
import org.apache.storm.generated.ExecutorStats;
import org.apache.storm.generated.SpoutStats;
import org.apache.storm.metric.internal.MultiLatencyStatAndMetric;
import org.apache.storm.metrics2.MetricPointForStormUi;
import org.apache.storm.metrics2.StormWorkerMetricRegistry;
import org.apache.storm.metricstore.UiWorkerMetricReporter;
import org.apache.storm.utils.Time;

@SuppressWarnings("unchecked")
public class SpoutExecutorStats extends CommonStats {
    private final MultiLatencyStatAndMetric completeLatencyStats;

    public SpoutExecutorStats(int rate, int numStatBuckets) {
        super(rate, numStatBuckets);
        this.completeLatencyStats = new MultiLatencyStatAndMetric(numStatBuckets);
    }

    public MultiLatencyStatAndMetric getCompleteLatencies() {
        return completeLatencyStats;
    }

    @Override
    public void cleanupStats() {
        completeLatencyStats.close();
        super.cleanupStats();
    }

    public void spoutAckedTuple(String stream, long latencyMs, Counter ackedCounter) {
        this.getAcked().incBy(stream, this.rate);
        ackedCounter.inc(this.rate);
        this.getCompleteLatencies().record(stream, latencyMs);
    }

    public void spoutFailedTuple(String stream, long latencyMs, Counter failedCounter) {
        this.getFailed().incBy(stream, this.rate);
        failedCounter.inc(this.rate);
    }

    @Override
    public ExecutorStats renderStats() {
        ExecutorStats ret = new ExecutorStats();
        // common fields
        ret.set_emitted(valueStat(getEmitted()));
        ret.set_transferred(valueStat(getTransferred()));
        ret.set_rate(this.rate);

        // spout stats
        SpoutStats spoutStats = new SpoutStats(
            valueStat(getAcked()), valueStat(getFailed()), valueStat(completeLatencyStats));
        ret.set_specific(ExecutorSpecificStats.spout(spoutStats));

        return ret;
    }

    private static final String EMITTED = "emitted";
    private static final String TRANSFERRED = "transferred";
    private static final String ACKED = "acked";
    private static final String FAILED = "failed";
    private static final String RATE = "rate";
    private static final String COMPLETE_LATENCY = "complete-latency";

    private static class MetricRegistrar {
        private final long timestampMs;
        private final String componentId;
        private final int taskId;

        public MetricRegistrar(long timestampMs, String componentId, int taskId) {
            this.timestampMs = timestampMs;
            this.componentId = componentId;
            this.taskId = taskId;
        }
        
        private MetricPointForStormUi createMetricPoint(String streamId, String metricName, double metricValue) {
            return new MetricPointForStormUi(timestampMs, componentId, taskId, streamId, metricName, metricValue);
        }
        
        private <T extends Number> List<MetricPointForStormUi> flattenMetrics(String metricName,
            Map<String, Map<String, T>> streamToTimeToMetricValue) {
            return streamToTimeToMetricValue.entrySet().stream()
                .flatMap(entry -> {
                    Map<String, T> timeToValue = entry.getValue();
                    String stream = entry.getKey();
                    return Arrays.asList(
                        createMetricPoint(stream, metricName + "-10-min", timeToValue.get("600").doubleValue()),
                        createMetricPoint(stream, metricName + "-3-hours", timeToValue.get("10800").doubleValue()),
                        createMetricPoint(stream, metricName + "-1-day", timeToValue.get("86400").doubleValue()),
                        createMetricPoint(stream, metricName + "-all-time", timeToValue.get(":all-time").doubleValue()))
                        .stream();
                })
                .collect(Collectors.toList());
        }
        
        private <T extends Number> void registerMetric(StormWorkerMetricRegistry registry, String metricName,
            Supplier<Map<String, Map<String, T>>> metricSupplier) {
            String reportedMetricName = UiWorkerMetricReporter.UI_METRIC_PREFIX
                + registry.metricNameForNimbus(metricName, componentId, taskId);
            registry.registry().gauge(reportedMetricName, () -> {
                return () -> {
                    return flattenMetrics(metricName, metricSupplier.get());
                };
            });
        }
    }

    public void registerMetrics(StormWorkerMetricRegistry registry) {
        long timestamp = Time.currentTimeMillis();
        String componentId = "TEMP";
        int taskId = 0;
        
        MetricRegistrar metricRegistrar = new MetricRegistrar(timestamp, componentId, taskId);
        
        metricRegistrar.registerMetric(registry, EMITTED, () -> getEmitted().getKeyToTimeToValue());
        metricRegistrar.registerMetric(registry, TRANSFERRED, () -> getTransferred().getKeyToTimeToValue());
        metricRegistrar.registerMetric(registry, ACKED, () -> getAcked().getKeyToTimeToValue());
        metricRegistrar.registerMetric(registry, FAILED, () -> getFailed().getKeyToTimeToValue());
        metricRegistrar.registerMetric(registry, COMPLETE_LATENCY, () -> getCompleteLatencies().getKeyToTimeToValue());
        registry.registry().gauge(registry.metricNameForNimbus(RATE, componentId, taskId), () -> {
            return () -> {
                return Collections.singletonList(new MetricPointForStormUi(timestamp,
                    componentId, taskId, RATE, this.rate));
            };
        });
        
    }
}

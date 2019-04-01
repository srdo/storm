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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.apache.storm.generated.ExecutorSpecificStats;
import org.apache.storm.generated.ExecutorStats;
import org.apache.storm.generated.SpoutStats;
import org.apache.storm.generated.WorkerMetricList2;
import org.apache.storm.generated.WorkerMetricPoint2;
import org.apache.storm.metric.internal.MultiLatencyStatAndMetric;
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
    private static final String DUMMY_STREAM_ID = "None";

    private static class MetricAdder {
        private final Map<String, WorkerMetricList2> streamToMetrics = new HashMap<>();
        private final long timestamp;
        private final String component;
        private final String executor;

        public MetricAdder(long timestamp, String component, String executor) {
            this.timestamp = timestamp;
            this.component = component;
            this.executor = executor;
        }

        public <T extends Number> void addMetrics(String metricName, Map<String, Map<String, T>> streamToTimeToMetricValue) {
            for (Entry<String, Map<String, T>> entry : streamToTimeToMetricValue.entrySet()) {
                Map<String, T> timeToValue = entry.getValue();
                String stream = entry.getKey();
                WorkerMetricList2 streamMetrics = streamToMetrics.computeIfAbsent(stream,
                    key -> new WorkerMetricList2(new ArrayList<>(), timestamp, component, executor, key));
                Stream.of(
                        new WorkerMetricPoint2(metricName + "-10-min", timeToValue.get("600").doubleValue()),
                        new WorkerMetricPoint2(metricName + "-3-hours", timeToValue.get("10800").doubleValue()),
                        new WorkerMetricPoint2(metricName + "-1-day", timeToValue.get("86400").doubleValue()),
                        new WorkerMetricPoint2(metricName + "-all-time", timeToValue.get(":all-time").doubleValue()))
                    .forEach(streamMetrics::add_to_metrics);
            }
        }
    }

    @Override
    public List<WorkerMetricList2> renderStats2() {
        long timestamp = Time.currentTimeMillis();
        String component = "TEMP";
        String executor = "TEMP";
        MetricAdder metricAdder = new MetricAdder(timestamp, component, executor);

        metricAdder.addMetrics(EMITTED, getEmitted().getKeyToTimeToValue());
        metricAdder.addMetrics(TRANSFERRED, getTransferred().getKeyToTimeToValue());
        metricAdder.addMetrics(ACKED, getAcked().getKeyToTimeToValue());
        metricAdder.addMetrics(FAILED, getFailed().getKeyToTimeToValue());
        metricAdder.addMetrics(COMPLETE_LATENCY, getCompleteLatencies().getKeyToTimeToValue());
        Map<String, WorkerMetricList2> streamToMetrics = metricAdder.streamToMetrics;
        streamToMetrics.put(DUMMY_STREAM_ID, 
            new WorkerMetricList2(Collections.singletonList(
                new WorkerMetricPoint2(RATE, this.rate)),
                timestamp, component, executor, DUMMY_STREAM_ID));

        return new ArrayList<>(streamToMetrics.values());
    }
}

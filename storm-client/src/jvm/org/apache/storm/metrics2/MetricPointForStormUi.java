
package org.apache.storm.metrics2;

import java.util.Optional;

public class MetricPointForStormUi {

    private final long timestampMs;
    private final String componentId;
    private final int taskId;
    private final String streamId;
    private final String metricName;
    private final double metricValue;

    public MetricPointForStormUi(long timestampMs, String componentId, int taskId, String streamId, String metricName, double metricValue) {
        this.timestampMs = timestampMs;
        this.componentId = componentId;
        this.taskId = taskId;
        this.streamId = streamId;
        this.metricName = metricName;
        this.metricValue = metricValue;
    }
    
    public MetricPointForStormUi(long timestampMs, String componentId, int taskId, String metricName, double metricValue) {
        this(timestampMs, componentId, taskId, null, metricName, metricValue);
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public String getComponentId() {
        return componentId;
    }

    public int getTaskId() {
        return taskId;
    }

    public Optional<String> getStreamId() {
        return Optional.ofNullable(streamId);
    }

    public String getMetricName() {
        return metricName;
    }

    public double getMetricValue() {
        return metricValue;
    }
}


package org.apache.storm.metricstore;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.storm.metrics2.MetricPointForStormUi;
import org.apache.storm.metrics2.filters.StormMetricsFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UiWorkerMetricReporter extends ScheduledReporter {
    
    private static final Logger LOG = LoggerFactory.getLogger(UiWorkerMetricReporter.class);
    private static final String REPORTER_NAME = "UiWorkerMetricReporter";
    
    private final String topologyId;
    private final int workerPort;
    private final WorkerMetricsProcessor workerMetricsProcessor;
    
    public static UiWorkerMetricReporter create(String topologyId, int workerPort, WorkerMetricsProcessor workerMetricsProcessor,
        MetricRegistry registry, TimeUnit rateUnit, TimeUnit durationUnit) {
        return new UiWorkerMetricReporter(topologyId, workerPort, workerMetricsProcessor, registry, MetricFilter.ALL,
            rateUnit, durationUnit);
    }

    private UiWorkerMetricReporter(String topologyId, int workerPort, WorkerMetricsProcessor workerMetricsProcessor,
        MetricRegistry registry, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {
        super(registry, REPORTER_NAME, filter, rateUnit, durationUnit);
        this.topologyId = topologyId;
        this.workerPort = workerPort;
        this.workerMetricsProcessor = workerMetricsProcessor;
    }
    
    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms,
        SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {

        List<MetricPointForStormUi> metricPoints = gauges.values().stream()
            .map(gauge -> (Gauge<List<MetricPointForStormUi>>) gauge)
            .flatMap(gauge -> gauge.getValue().stream())
            .collect(Collectors.toList());
        try {
            workerMetricsProcessor.processWorkerMetrics(metricPoints, topologyId, workerPort);
        } catch (MetricException e) {
            //Just log without crashing, the topology can run without delivering metrics.
            LOG.error("Failed to send metrics to Nimbus", e);
        }
        
    }
}

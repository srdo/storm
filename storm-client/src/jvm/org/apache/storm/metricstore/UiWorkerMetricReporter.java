
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
    //STORM-3367: Upgrade to Dropwizard 5 and replace this with a tag
    public static final String UI_METRIC_PREFIX = "nimbus-ui-";
    
    private final String topologyId;
    private final int workerId;
    private final WorkerMetricsProcessor workerMetricsProcessor;
    
    public static UiWorkerMetricReporter create(String topologyId, int workerId, WorkerMetricsProcessor workerMetricsProcessor,
        MetricRegistry registry, String name, TimeUnit rateUnit, TimeUnit durationUnit) {
        return new UiWorkerMetricReporter(topologyId, workerId, workerMetricsProcessor, registry, name, new OnlyUiGaugesMetricFilter(),
            rateUnit, durationUnit);
    }

    private UiWorkerMetricReporter(String topologyId, int workerId, WorkerMetricsProcessor workerMetricsProcessor,
        MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {
        super(registry, name, filter, rateUnit, durationUnit);
        this.topologyId = topologyId;
        this.workerId = workerId;
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
            workerMetricsProcessor.processWorkerMetrics(metricPoints, topologyId, workerId);
        } catch (MetricException e) {
            //Just log without crashing, the topology can run without delivering metrics.
            LOG.error("Failed to send metrics to Nimbus", e);
        }
        
    }
    
    private static class OnlyUiGaugesMetricFilter implements MetricFilter {

        @Override
        public boolean matches(String name, Metric metric) {
            return metric instanceof Gauge && name.startsWith(UI_METRIC_PREFIX);
        }
        
    }
    
    /**
     * Wraps another StormMetricFilter (may be null), while filtering out the UI gauges accepted by this reporter.
     */
    public static class DiscardUiGaugesMetricFilter implements StormMetricsFilter {
        private final StormMetricsFilter delegate;
        private final MetricFilter onlyUiGaugesFilter = new OnlyUiGaugesMetricFilter();

        public DiscardUiGaugesMetricFilter(StormMetricsFilter delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean matches(String name, Metric metric) {
            return !onlyUiGaugesFilter.matches(name, metric) && delegate == null ? true : delegate.matches(name, metric);
        }

        @Override
        public void prepare(Map<String, Object> config) {
            if (delegate != null) {
                delegate.prepare(config);
            }
        }
    }
}


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
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.storm.generated.WorkerMetrics2;
import org.apache.storm.metrics2.MetricPointForNimbus;

public class NimbusWorkerMetricReporter extends ScheduledReporter {
    
    //STORM-3367: Upgrade to Dropwizard 5 and replace this with a tag
    public static final String UI_METRIC_PREFIX = "nimbus-ui-";
    
    private final String topologyId;
    private final String workerId;
    private final String hostName;
    
    public static NimbusWorkerMetricReporter create(String topologyId, String workerId, String hostName, 
        MetricRegistry registry, String name, TimeUnit rateUnit, TimeUnit durationUnit) {
        return new NimbusWorkerMetricReporter(topologyId, workerId, hostName, registry, name, new OnlyUiGaugesMetricFilter(),
            rateUnit, durationUnit);
    }

    private NimbusWorkerMetricReporter(String topologyId, String workerId, String hostName, 
        MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {
        super(registry, name, filter, rateUnit, durationUnit);
        this.topologyId = topologyId;
        this.workerId = workerId;
        this.hostName = hostName;
    }
    
    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms,
        SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
        
        WorkerMetrics2 workerMetrics = new WorkerMetrics2();
        
        List<MetricPointForNimbus> metricPoints = gauges.values().stream()
            .map(gauge -> (Gauge<List<MetricPointForNimbus>>)gauge)
            .flatMap(gauge -> gauge.getValue().stream())
            .collect(Collectors.toList());
        
        metricPoints.stream()
            .collect(Collectors.groupingBy(classifier))
        
    }
    
    private static class OnlyUiGaugesMetricFilter implements MetricFilter {

        @Override
        public boolean matches(String name, Metric metric) {
            return metric instanceof Gauge && name.startsWith(UI_METRIC_PREFIX);
        }
        
    }
}


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
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class NimbusMetricReporter extends ScheduledReporter {
    
    //STORM-3367: Upgrade to Dropwizard 5 and replace this with a tag
    public static final String UI_METRIC_PREFIX = "nimbus-ui-";
    
    public static NimbusMetricReporter create(MetricRegistry registry, String name, TimeUnit rateUnit, TimeUnit durationUnit) {
        return new NimbusMetricReporter(registry, name, new OnlyUiGaugesMetricFilter(), rateUnit, durationUnit);
    }
    
    private NimbusMetricReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {
        super(registry, name, filter, rateUnit, durationUnit);
    }
    
    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms,
        SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
        
    }
    
    private static class OnlyUiGaugesMetricFilter implements MetricFilter {

        @Override
        public boolean matches(String name, Metric metric) {
            return metric instanceof Gauge && name.startsWith(UI_METRIC_PREFIX);
        }
        
    }
}

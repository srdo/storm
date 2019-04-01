
package org.apache.storm.metricstore;

import java.util.Map;
import org.apache.storm.Config;

public class MetricProcessorConfig {

    /**
     * Configures metric processor to use the class specified in the conf.
     *
     * @param conf the daemon or topology config
     * @return WorkerMetricsProcessor prepared processor
     * @throws MetricException on misconfiguration
     */
    public static WorkerMetricsProcessor configureMetricProcessor(Map conf) throws MetricException {

        try {
            String processorClass = (String) conf.get(Config.STORM_METRIC_PROCESSOR_CLASS);
            WorkerMetricsProcessor processor = (WorkerMetricsProcessor) (Class.forName(processorClass)).newInstance();
            processor.prepare(conf);
            return processor;
        } catch (Exception e) {
            throw new MetricException("Failed to create metric processor", e);
        }
    }
    
}

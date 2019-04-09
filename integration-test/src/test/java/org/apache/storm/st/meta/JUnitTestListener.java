
package org.apache.storm.st.meta;

import org.apache.commons.lang.StringUtils;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JUnitTestListener implements TestExecutionListener {

    private static final Logger LOG = LoggerFactory.getLogger(JUnitTestListener.class);
    private final String hr = StringUtils.repeat("-", 100);
    
    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
    }

    @Override
    public void dynamicTestRegistered(TestIdentifier testIdentifier) {
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        LOG.info(hr);
        LOG.info(String.format("Testing going to start for: %s", testIdentifier.getDisplayName()));
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        LOG.info(
                String.format("Testing going to end for: %s ----- Status: %s", testIdentifier.getDisplayName(), testExecutionResult));
        LOG.info(hr);
    }

    @Override
    public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
    }
}

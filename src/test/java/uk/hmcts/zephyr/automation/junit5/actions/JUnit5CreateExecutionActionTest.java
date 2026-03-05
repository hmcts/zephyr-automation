package uk.hmcts.zephyr.automation.junit5.actions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.junit5.JiraAnnotationMetadata;
import uk.hmcts.zephyr.automation.junit5.model.Junit5ZephyrReport;
import uk.hmcts.zephyr.automation.support.TestUtil;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class JUnit5CreateExecutionActionTest {

    @BeforeEach
    void setUp() throws Exception {
        TestUtil.resetSingletons();
        Config.instantiate(
            TestUtil.defaultArgs(Config.ActionType.CREATE_EXECUTION, Config.ProcessType.JUNIT5_JSON_REPORT));
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtil.resetSingletons();
    }

    @Test
    void givenNullReport_whenProcess_thenSkipsProcessing() {
        JUnit5CreateExecutionAction action = spy(new JUnit5CreateExecutionAction());
        doReturn(null).when(action).getJunit5ZephyrReport();
        doReturn(List.of()).when(action).processTests(anyList());

        action.process();

        verify(action).getJunit5ZephyrReport();
        verify(action, never()).processTests(anyList());
    }

    @Test
    void givenReportWithTests_whenProcess_thenDelegatesToProcessTests() {
        JUnit5CreateExecutionAction action = spy(new JUnit5CreateExecutionAction());
        Junit5ZephyrReport report = new Junit5ZephyrReport();
        Junit5ZephyrReport.Test first = new Junit5ZephyrReport.Test(
            "ID-1",
            "First test",
            "uk.hmcts.ClassOne",
            "firstCase",
            Junit5ZephyrReport.Test.Status.PASSED,
            null,
            null,
            Set.of(),
            JiraAnnotationMetadata.empty()
        );
        Junit5ZephyrReport.Test second = new Junit5ZephyrReport.Test(
            "ID-2",
            "Second test",
            "uk.hmcts.ClassTwo",
            "secondCase",
            Junit5ZephyrReport.Test.Status.FAILED,
            null,
            null,
            Set.of(),
            JiraAnnotationMetadata.empty()
        );
        report.getTests().add(first);
        report.getTests().add(second);

        doReturn(report).when(action).getJunit5ZephyrReport();
        doReturn(List.of()).when(action).processTests(anyList());

        action.process();

        verify(action).getJunit5ZephyrReport();
        verify(action).processTests(report.getTests());
    }
}

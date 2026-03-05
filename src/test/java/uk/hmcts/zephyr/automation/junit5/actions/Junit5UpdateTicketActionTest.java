package uk.hmcts.zephyr.automation.junit5.actions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.junit5.JiraAnnotationMetadata;
import uk.hmcts.zephyr.automation.junit5.model.Junit5ZephyrReport;
import uk.hmcts.zephyr.automation.support.TestUtil;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class Junit5UpdateTicketActionTest {

    @BeforeEach
    void setUp() throws Exception {
        TestUtil.resetSingletons();
        Config.instantiate(TestUtil.defaultArgs(
            Config.ActionType.UPDATE_TICKETS, Config.ProcessType.JUNIT5_JSON_REPORT));
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtil.resetSingletons();
    }

    @Test
    void givenNullReport_whenProcess_thenSkipsUpdates() {
        Junit5UpdateTicketAction action = spy(new Junit5UpdateTicketAction());
        doReturn(null).when(action).getJunit5ZephyrReport();

        action.process();

        verify(action).getJunit5ZephyrReport();
        verify(action, never()).updateJiraIssue(any());
    }

    @Test
    void givenReportWithTests_whenProcess_thenUpdatesEachTest() {
        Junit5UpdateTicketAction action = spy(new Junit5UpdateTicketAction());
        Junit5ZephyrReport report = new Junit5ZephyrReport();
        Junit5ZephyrReport.Test first = sampleTest("ID-1", "ClassOne", "methodOne");
        Junit5ZephyrReport.Test second = sampleTest("ID-2", "ClassTwo", "methodTwo");
        report.getTests().add(first);
        report.getTests().add(second);

        doReturn(report).when(action).getJunit5ZephyrReport();
        doNothing().when(action).updateJiraIssue(any());

        action.process();

        verify(action).getJunit5ZephyrReport();
        verify(action).updateJiraIssue(first);
        verify(action).updateJiraIssue(second);
    }

    @Test
    void givenReportContainingNullTests_whenProcess_thenSkipsNullEntries() {
        Junit5UpdateTicketAction action = spy(new Junit5UpdateTicketAction());
        Junit5ZephyrReport report = new Junit5ZephyrReport();
        Junit5ZephyrReport.Test valid = sampleTest("ID-3", "ClassThree", "methodThree");
        report.getTests().add(null);
        report.getTests().add(valid);

        doReturn(report).when(action).getJunit5ZephyrReport();
        doNothing().when(action).updateJiraIssue(any());

        action.process();

        verify(action).getJunit5ZephyrReport();
        verify(action, never()).updateJiraIssue(null);
        verify(action).updateJiraIssue(valid);
    }

    private Junit5ZephyrReport.Test sampleTest(String id, String className, String methodName) {
        return new Junit5ZephyrReport.Test(
            id,
            "display-" + id,
            className,
            methodName,
            Junit5ZephyrReport.Test.Status.PASSED,
            null,
            null,
            Set.of(),
            JiraAnnotationMetadata.empty()
        );
    }
}

package uk.hmcts.zephyr.automation.junit5.actions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.junit5.JiraAnnotationMetadata;
import uk.hmcts.zephyr.automation.junit5.model.Junit5ZephyrReport;
import uk.hmcts.zephyr.automation.support.TestUtil;
import uk.hmcts.zephyr.automation.util.FileUtil;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class Junit5CreateTicketActionTest {

    private MockedStatic<FileUtil> fileUtilMock;

    @BeforeEach
    void setUp() throws Exception {
        fileUtilMock = mockStatic(FileUtil.class);
        TestUtil.resetSingletons();
        Config.instantiate(TestUtil.defaultArgs(
            Config.ActionType.CREATE_TICKETS, Config.ProcessType.JUNIT5_JSON_REPORT));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (fileUtilMock != null) {
            fileUtilMock.close();
            fileUtilMock = null;
        }
        TestUtil.resetSingletons();
    }

    @Test
    void givenNullReport_whenProcess_thenSkipsCreationAndWrite() {
        Junit5CreateTicketAction action = spy(new Junit5CreateTicketAction());
        doReturn(null).when(action).getJunit5ZephyrReport();

        action.process();

        verify(action).getJunit5ZephyrReport();
        verify(action, never()).createJiraIssue(any());
        fileUtilMock.verify(() -> FileUtil.writeToFile(any(), any()), never());
    }

    @Test
    void givenReportWithTests_whenProcess_thenCreatesTicketsAndWritesReport() {
        Junit5CreateTicketAction action = spy(new Junit5CreateTicketAction());
        Junit5ZephyrReport report = new Junit5ZephyrReport();
        report.getTests().add(sampleTest("ID-1", "ClassOne", "methodOne"));
        report.getTests().add(sampleTest("ID-2", "ClassTwo", "methodTwo"));

        doReturn(report).when(action).getJunit5ZephyrReport();
        doReturn(Optional.empty()).when(action).createJiraIssue(any());

        action.process();

        verify(action).getJunit5ZephyrReport();
        verify(action).createJiraIssue(report.getTests().get(0));
        verify(action).createJiraIssue(report.getTests().get(1));
        fileUtilMock.verify(() -> FileUtil.writeToFile(Config.getReportPath(), report));
    }

    @Test
    void givenReportContainingNullTests_whenProcess_thenSkipsNullEntries() {
        Junit5CreateTicketAction action = spy(new Junit5CreateTicketAction());
        Junit5ZephyrReport report = new Junit5ZephyrReport();
        Junit5ZephyrReport.Test validTest = sampleTest("ID-3", "ClassThree", "methodThree");
        report.getTests().add(null);
        report.getTests().add(validTest);

        doReturn(report).when(action).getJunit5ZephyrReport();
        doReturn(Optional.empty()).when(action).createJiraIssue(any());

        action.process();

        verify(action).getJunit5ZephyrReport();
        verify(action, never()).createJiraIssue(null);
        verify(action).createJiraIssue(validTest);
        fileUtilMock.verify(() -> FileUtil.writeToFile(Config.getReportPath(), report));
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

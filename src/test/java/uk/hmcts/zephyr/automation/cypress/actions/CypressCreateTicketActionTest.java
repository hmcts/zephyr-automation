package uk.hmcts.zephyr.automation.cypress.actions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cypress.models.CypressReport;
import uk.hmcts.zephyr.automation.support.TestUtil;
import uk.hmcts.zephyr.automation.util.FileUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CypressCreateTicketActionTest {

    private MockedStatic<FileUtil> fileUtilMock;

    @BeforeEach
    void setUp() throws Exception {
        fileUtilMock = mockStatic(FileUtil.class);
        TestUtil.resetSingletons();
        Config.instantiate(TestUtil.defaultArgs(
            Config.ActionType.CREATE_TICKETS, Config.ProcessType.CYPRESS_JSON_REPORT));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (fileUtilMock != null) {
            fileUtilMock.close();
            fileUtilMock = null;
        }
        TestUtil.resetSingletons();
    }

    @Nested
    class ProcessTest {

        @Test
        void given_reportWithTests_when_process_then_createsTicketsAndWritesReport() {
            final CypressCreateTicketAction action = spy(new CypressCreateTicketAction());
            CypressReport report = new CypressReport();
            List<CypressReport.CypressTest> tests =
                List.of(new CypressReport.CypressTest(), new CypressReport.CypressTest());
            tests.getFirst().setTitle("first-test");
            tests.get(1).setTitle("second-test");
            report.setTests(tests);

            doReturn(Optional.empty()).when(action).createJiraIssue(any());

            doReturn(report).when(action).getCypressReport();

            action.process();

            verify(action).getCypressReport();
            verify(action).createJiraIssue(tests.getFirst());
            verify(action).createJiraIssue(tests.get(1));
            fileUtilMock.verify(() -> FileUtil.writeToFile(Config.getReportPath(), report));
        }

        @Test
        void given_reportWithoutTests_when_process_then_skipsCreationAndWritesReport() {
            CypressCreateTicketAction action = spy(new CypressCreateTicketAction());
            CypressReport report = mock(CypressReport.class);

            doReturn(report).when(action).getCypressReport();
            when(report.getTests()).thenReturn(Collections.emptyList());

            action.process();

            verify(action).getCypressReport();
            verify(report).getTests();
            verify(action, never()).createJiraIssue(any());
            fileUtilMock.verifyNoInteractions();
        }
    }
}

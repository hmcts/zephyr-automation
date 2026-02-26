package uk.hmcts.zephyr.automation.cypress.actions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cypress.models.CypressReport;
import uk.hmcts.zephyr.automation.support.TestUtil;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CypressUpdateTicketActionTest {

    @BeforeEach
    void setUp() throws Exception {
        TestUtil.resetSingletons();
        Config.instantiate(TestUtil.defaultArgs(
            Config.ActionType.UPDATE_TICKETS,
            Config.ProcessType.CYPRESS_JSON_REPORT
        ));
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtil.resetSingletons();
    }


    @Nested
    class ProcessTest {

        @Test
        void given_reportWithTests_when_process_then_updatesEachIssue() {
            final CypressUpdateTicketAction action = spy(new CypressUpdateTicketAction());
            CypressReport report = new CypressReport();
            List<CypressReport.CypressTest> tests =
                List.of(new CypressReport.CypressTest(), new CypressReport.CypressTest());
            tests.get(0).setTitle("first-test");
            tests.get(1).setTitle("second-test");
            report.setTests(tests);

            doReturn(report).when(action).getCypressReport();
            doNothing().when(action).updateJiraIssue(any());

            action.process();

            verify(action).getCypressReport();
            verify(action).updateJiraIssue(tests.get(0));
            verify(action).updateJiraIssue(tests.get(1));
        }

        @Test
        void given_reportWithoutTests_when_process_then_skipsUpdates() {
            CypressUpdateTicketAction action = spy(new CypressUpdateTicketAction());
            CypressReport report = mock(CypressReport.class);

            doReturn(report).when(action).getCypressReport();
            doReturn(Collections.emptyList()).when(report).getTests();

            action.process();

            verify(action).getCypressReport();
            verify(report).getTests();
            verify(action, never()).updateJiraIssue(any());
        }
    }
}

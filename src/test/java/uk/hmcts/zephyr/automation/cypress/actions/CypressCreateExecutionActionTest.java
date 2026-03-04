package uk.hmcts.zephyr.automation.cypress.actions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cypress.models.CypressReport;
import uk.hmcts.zephyr.automation.support.TestUtil;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CypressCreateExecutionActionTest {

    @BeforeEach
    void setUp() throws Exception {
        TestUtil.resetSingletons();
        Config.instantiate(
            TestUtil.defaultArgs(Config.ActionType.CREATE_EXECUTION, Config.ProcessType.CYPRESS_JSON_REPORT));
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtil.resetSingletons();
    }


    @Nested
    class ProcessTest {

        @Test
        void given_reportWithTests_when_process_then_delegatesToProcessTests() {
            final CypressCreateExecutionAction action = spy(new CypressCreateExecutionAction());
            CypressReport report = new CypressReport();
            List<CypressReport.CypressTest> tests =
                List.of(new CypressReport.CypressTest(), new CypressReport.CypressTest());
            tests.getFirst().setTitle("first-test");
            tests.get(1).setTitle("second-test");
            report.setTests(tests);

            doReturn(report).when(action).getCypressReport();
            doReturn(List.of()).when(action).processTests(anyList());

            action.process();

            verify(action).getCypressReport();
            verify(action).processTests(tests);
        }

        @Test
        void given_reportWithoutTests_when_process_then_invokesProcessTestsWithEmptyList() {
            CypressCreateExecutionAction action = spy(new CypressCreateExecutionAction());
            CypressReport report = mock(CypressReport.class);

            doReturn(report).when(action).getCypressReport();
            when(report.getTests()).thenReturn(Collections.emptyList());
            doReturn(List.of()).when(action).processTests(anyList());

            action.process();

            ArgumentCaptor<List<CypressReport.CypressTest>> captor = ArgumentCaptor.captor();
            verify(action).getCypressReport();
            verify(action).processTests(captor.capture());
            assertTrue(captor.getValue().isEmpty());
        }
    }
}

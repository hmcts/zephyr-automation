package uk.hmcts.zephyr.automation.cucumber.actions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element;
import uk.hmcts.zephyr.automation.support.CucumberDataUtil;
import uk.hmcts.zephyr.automation.support.TestUtil;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class CucumberUpdateTicketActionTest {

    @BeforeEach
    void setUp() throws Exception {
        TestUtil.resetSingletons();
        Config.instantiate(TestUtil.defaultArgs(
            Config.ActionType.UPDATE_TICKETS,
            Config.ProcessType.CUCUMBER_JSON_REPORT
        ));
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtil.resetSingletons();
    }

    @Nested
    class ProcessTest {

        @Test
        void given_featuresWithScenarios_when_process_then_updatesOnlyScenarioElements() {
            CucumberUpdateTicketAction action = spy(new CucumberUpdateTicketAction());
            Element scenarioOne = CucumberDataUtil.scenario("Scenario 1");
            Element scenarioTwo = CucumberDataUtil.scenario("Scenario 2");
            Element background = CucumberDataUtil.element("background");
            List<CucumberFeature> features = List.of(
                CucumberDataUtil.featureWithElements(scenarioOne, background),
                CucumberDataUtil.featureWithElements(scenarioTwo)
            );

            doReturn(features).when(action).getFeatures();
            doNothing().when(action).updateJiraIssue(any());

            action.process();

            verify(action).getFeatures();
            verify(action).updateJiraIssue(scenarioOne);
            verify(action).updateJiraIssue(scenarioTwo);
            verify(action, never()).updateJiraIssue(background);
        }

        @Test
        void given_featureWithoutElements_when_process_then_skipsFeature() {
            CucumberUpdateTicketAction action = spy(new CucumberUpdateTicketAction());
            CucumberFeature emptyFeature = new CucumberFeature();
            emptyFeature.setElements(null);

            doReturn(List.of(emptyFeature)).when(action).getFeatures();
            doNothing().when(action).updateJiraIssue(any());

            action.process();

            verify(action).getFeatures();
            verify(action, never()).updateJiraIssue(any());
        }
    }
}


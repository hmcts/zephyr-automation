package uk.hmcts.zephyr.automation.cucumber.actions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element;
import uk.hmcts.zephyr.automation.support.CucumberDataUtil;
import uk.hmcts.zephyr.automation.support.TestUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class CucumberCreateExecutionActionTest {

    @BeforeEach
    void setUp() throws Exception {
        TestUtil.resetSingletons();
        Config.instantiate(
            TestUtil.defaultArgs(Config.ActionType.CREATE_EXECUTION, Config.ProcessType.CUCUMBER_JSON_REPORT));
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtil.resetSingletons();
    }

    @Nested
    class ProcessTest {

        @Test
        void given_noFeatures_when_process_then_skipsProcessing() {
            CucumberCreateExecutionAction action = spy(new CucumberCreateExecutionAction());
            doReturn(null).when(action).getFeatures();
            doNothing().when(action).processTests(anyList());

            action.process();

            verify(action).getFeatures();
            verify(action, never()).processTests(anyList());
        }

        @Test
        void given_emptyFeatures_when_process_then_skipsProcessing() {
            CucumberCreateExecutionAction action = spy(new CucumberCreateExecutionAction());
            doReturn(Collections.emptyList()).when(action).getFeatures();
            doNothing().when(action).processTests(anyList());

            action.process();

            verify(action).getFeatures();
            verify(action, never()).processTests(anyList());
        }

        @Test
        void given_featuresWithScenarios_when_process_then_filtersAndDelegates() {
            CucumberCreateExecutionAction action = spy(new CucumberCreateExecutionAction());
            List<CucumberFeature> features = new ArrayList<>();
            features.add(CucumberDataUtil.featureWithElements("Feature A", CucumberDataUtil.scenario("Scenario 1"), CucumberDataUtil.background()));
            features.add(CucumberDataUtil.featureWithElements("Feature B", CucumberDataUtil.scenario("Scenario 2")));

            doReturn(features).when(action).getFeatures();
            doNothing().when(action).processTests(anyList());

            action.process();

            ArgumentCaptor<List<Element>> captor = ArgumentCaptor.captor();
            verify(action).processTests(captor.capture());

            List<Element> forwarded = captor.getValue();
            assertEquals(2, forwarded.size());
            assertTrue(forwarded.stream().allMatch(element -> element.getType().equals("scenario")));
            assertEquals("Scenario 1", forwarded.get(0).getName());
            assertEquals("Scenario 2", forwarded.get(1).getName());
        }
    }
}


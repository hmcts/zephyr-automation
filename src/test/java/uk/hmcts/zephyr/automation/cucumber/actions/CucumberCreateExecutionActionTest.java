package uk.hmcts.zephyr.automation.cucumber.actions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element;
import uk.hmcts.zephyr.automation.support.CucumberDataUtil;
import uk.hmcts.zephyr.automation.support.TestUtil;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionSearchResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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
            doReturn(List.of()).when(action).processTests(anyList());

            action.process();

            verify(action).getFeatures();
            verify(action, never()).processTests(anyList());
        }

        @Test
        void given_emptyFeatures_when_process_then_skipsProcessing() {
            CucumberCreateExecutionAction action = spy(new CucumberCreateExecutionAction());
            doReturn(Collections.emptyList()).when(action).getFeatures();
            doReturn(List.of()).when(action).processTests(anyList());

            action.process();

            verify(action).getFeatures();
            verify(action, never()).processTests(anyList());
        }

        @Test
        void given_featuresWithScenarios_when_process_then_filtersAndDelegates() {
            CucumberCreateExecutionAction action = spy(new CucumberCreateExecutionAction());
            List<CucumberFeature> features = new ArrayList<>();
            features.add(CucumberDataUtil.featureWithElements("Feature A",
                CucumberDataUtil.scenario("Scenario 1"), CucumberDataUtil.background()));
            features.add(CucumberDataUtil.featureWithElements("Feature B",
                CucumberDataUtil.scenario("Scenario 2")));

            doReturn(features).when(action).getFeatures();
            doReturn(List.of()).when(action).processTests(anyList());

            action.process();

            ArgumentCaptor<List<Element>> captor = ArgumentCaptor.captor();
            verify(action).processTests(captor.capture());

            List<Element> forwarded = captor.getValue();
            assertEquals(2, forwarded.size());
            assertTrue(forwarded.stream().allMatch(element -> element.getType().equals("scenario")));
            assertEquals("Scenario 1", forwarded.getFirst().getName());
            assertEquals("Scenario 2", forwarded.get(1).getName());
        }
    }

    @Nested
    @DisplayName("processEmbeddings")
    class ProcessEmbeddingsTests {

        @Test
        void given_elementWithNoSteps_when_processEmbeddings_then_skipsProcessing() {
            CucumberCreateExecutionAction action = spy(new CucumberCreateExecutionAction());
            Element element = new Element();
            element.setSteps(null);

            ZephyrExecutionSearchResponse.Execution execution = mock(ZephyrExecutionSearchResponse.Execution.class);
            action.processEmbeddings(element, execution);

            verify(action).getEmbeddings(element);
            verify(action, never()).processEmbedding(any(), any());
        }

        @Test
        void given_elementWithStepsButNoEmbeddings_when_processEmbeddings_then_skipsProcessing() {
            CucumberCreateExecutionAction action = spy(new CucumberCreateExecutionAction());
            Element element = new Element();
            Element.Step step = new Element.Step();
            step.setEmbeddings(null);
            element.setSteps(List.of(step));

            ZephyrExecutionSearchResponse.Execution execution = mock(ZephyrExecutionSearchResponse.Execution.class);
            action.processEmbeddings(element, execution);

            verify(action).getEmbeddings(element);
            verify(action, never()).processEmbedding(any(), any());
        }

        @Test
        void given_elementWithEmbeddings_when_processEmbeddings_then_processesEachEmbedding() {
            CucumberCreateExecutionAction action = spy(new CucumberCreateExecutionAction());
            Element element = new Element();
            Element.Step step1 = new Element.Step();
            Element.Step.Embedding step1Embedding = mock(Element.Step.Embedding.class);
            step1.setEmbeddings(List.of(step1Embedding));
            Element.Step step2 = new Element.Step();
            Element.Step.Embedding step2Embedding1 = mock(Element.Step.Embedding.class);
            Element.Step.Embedding step2Embedding2 = mock(Element.Step.Embedding.class);

            step2.setEmbeddings(List.of(step2Embedding1, step2Embedding2));
            element.setSteps(List.of(step1, step2));

            ZephyrExecutionSearchResponse.Execution execution = mock(ZephyrExecutionSearchResponse.Execution.class);
            doNothing().when(action).processEmbedding(any(), any());
            action.processEmbeddings(element, execution);

            verify(action).getEmbeddings(element);
            verify(action).processEmbedding(execution, step1Embedding);
            verify(action).processEmbedding(execution, step2Embedding1);
            verify(action).processEmbedding(execution, step2Embedding2);
        }
    }

    @Nested
    @DisplayName("processEmbedding")
    class ProcessEmbeddingTests {

        @Test
        void given_embedding_when_processEmbedding_then_attachesToExecution() {
            CucumberCreateExecutionAction action = spy(new CucumberCreateExecutionAction());
            Element.Step.Embedding embedding = new Element.Step.Embedding();
            ZephyrExecutionSearchResponse.Execution execution = new ZephyrExecutionSearchResponse.Execution();
            execution.setId(123L);
            execution.setIssueKey("TEST-123");

            doNothing().when(action).attachFileToExecution(any(), any());
            action.processEmbedding(execution, embedding);

            verify(action).attachFileToExecution(123L, embedding);
        }
    }

    @Nested
    @DisplayName("hasEmbeddings")
    class HasEmbeddingsTests {

        @Test
        void given_elementWithNoSteps_when_hasEmbeddings_then_returnsFalse() {
            CucumberCreateExecutionAction action = new CucumberCreateExecutionAction();
            Element element = new Element();
            element.setSteps(null);

            boolean result = action.hasEmbeddings(element);
            assertFalse(result);
        }

        @Test
        void given_elementWithStepsButNoEmbeddings_when_hasEmbeddings_then_returnsFalse() {
            CucumberCreateExecutionAction action = new CucumberCreateExecutionAction();
            Element element = new Element();
            Element.Step step = new Element.Step();
            step.setEmbeddings(List.of());
            element.setSteps(List.of(step));

            boolean result = action.hasEmbeddings(element);
            assertFalse(result);
        }

        @Test
        void given_elementWithStepsButEmptyEmbeddings_when_hasEmbeddings_then_returnsFalse() {
            CucumberCreateExecutionAction action = new CucumberCreateExecutionAction();
            Element element = new Element();
            Element.Step step = new Element.Step();
            step.setEmbeddings(null);
            element.setSteps(List.of(step));

            boolean result = action.hasEmbeddings(element);
            assertFalse(result);
        }

        @Test
        void given_elementWithAtLeastOneEmbedding_when_hasEmbeddings_then_returnsTrue() {
            CucumberCreateExecutionAction action = new CucumberCreateExecutionAction();
            Element element = new Element();
            Element.Step step1 = new Element.Step();
            step1.setEmbeddings(null);
            Element.Step step2 = new Element.Step();
            Element.Step.Embedding embedding = new Element.Step.Embedding();
            step2.setEmbeddings(List.of(embedding));
            element.setSteps(List.of(step1, step2));

            boolean result = action.hasEmbeddings(element);
            assertTrue(result);
        }

    }

    @Nested
    @DisplayName("getEmbeddings")
    class GetEmbeddingsTests {

        @Test
        void given_elementWithNullSteps_when_getEmbeddings_then_returnsEmptyList() {
            CucumberCreateExecutionAction action = new CucumberCreateExecutionAction();
            Element element = new Element();
            element.setSteps(null);

            List<Element.Step.Embedding> result = action.getEmbeddings(element);
            assertTrue(result.isEmpty());
        }

        @Test
        void given_elementWithEmptySteps_when_getEmbeddings_then_returnsEmptyList() {
            CucumberCreateExecutionAction action = new CucumberCreateExecutionAction();
            Element element = new Element();
            element.setSteps(List.of());

            List<Element.Step.Embedding> result = action.getEmbeddings(element);
            assertTrue(result.isEmpty());
        }

        @Test
        void given_elementWithStepsButNullEmbeddings_when_getEmbeddings_then_returnsEmptyList() {
            CucumberCreateExecutionAction action = new CucumberCreateExecutionAction();
            Element element = new Element();
            Element.Step step = new Element.Step();
            step.setEmbeddings(null);
            element.setSteps(List.of(step));

            List<Element.Step.Embedding> result = action.getEmbeddings(element);
            assertTrue(result.isEmpty());
        }

        @Test
        void given_elementWithStepsButEmptyEmbeddings_when_getEmbeddings_then_returnsEmptyList() {
            CucumberCreateExecutionAction action = new CucumberCreateExecutionAction();
            Element element = new Element();
            Element.Step step = new Element.Step();
            step.setEmbeddings(List.of());
            element.setSteps(List.of(step));

            List<Element.Step.Embedding> result = action.getEmbeddings(element);
            assertTrue(result.isEmpty());
        }

        @Test
        void given_elementWithStepsAndEmbeddings_when_getEmbeddings_then_returnsAllEmbeddings() {
            CucumberCreateExecutionAction action = new CucumberCreateExecutionAction();
            Element element = new Element();
            Element.Step step1 = new Element.Step();
            Element.Step.Embedding embedding1 = new Element.Step.Embedding();
            step1.setEmbeddings(List.of(embedding1));
            Element.Step step2 = new Element.Step();
            Element.Step.Embedding embedding2 = new Element.Step.Embedding();
            step2.setEmbeddings(List.of(embedding2));
            element.setSteps(List.of(step1, step2));

            List<Element.Step.Embedding> result = action.getEmbeddings(element);
            assertEquals(2, result.size());
            assertTrue(result.contains(embedding1));
            assertTrue(result.contains(embedding2));
        }

    }
}

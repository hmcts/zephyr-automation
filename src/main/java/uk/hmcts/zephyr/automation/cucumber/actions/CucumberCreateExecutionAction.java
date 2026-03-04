package uk.hmcts.zephyr.automation.cucumber.actions;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.actions.AbstractCreateExecutionAction;
import uk.hmcts.zephyr.automation.cucumber.CucumberTagService;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element;
import uk.hmcts.zephyr.automation.util.Util;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionSearchResponse;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CucumberCreateExecutionAction
    extends AbstractCreateExecutionAction<Element>
    implements CucumberAction {

    public CucumberCreateExecutionAction() {
        super(new CucumberTagService());
    }

    @Override
    public void process() {
        log.info("Starting Cucumber Create Execution Action");
        List<CucumberFeature> cucumberFeatures = getFeatures();
        if (cucumberFeatures == null || cucumberFeatures.isEmpty()) {
            log.warn("No features found to process for execution creation");
            return;
        }
        List<Element> tests = cucumberFeatures.stream()
            .map(CucumberFeature::getElements)
            .flatMap(List::stream)
            .filter(getElementFilter())
            .toList();

        List<ScenarioResult> scenarioResults = processTests(tests);
        if (Config.shouldAttachEvidence()) {
            scenarioResults.stream()
                .filter(scenarioResult -> hasEmbeddings(scenarioResult.getTest()))
                .filter(scenarioResult -> scenarioResult.getExecutionDetail() != null)
                .forEach(scenarioResult ->
                    processEmbeddings(scenarioResult.getTest(), scenarioResult.getExecutionDetail()));
        }
    }

    void processEmbeddings(Element element, ZephyrExecutionSearchResponse.Execution executionDetail) {
        List<Element.Step.Embedding> embeddings = getEmbeddings(element);
        if (embeddings.isEmpty()) {
            log.debug("No embeddings found for element: {}", element.getName());
            return;
        }
        log.info("Processing {} embeddings for element: {}", embeddings.size(), executionDetail.getIssueKey());
        for (Element.Step.Embedding embedding : embeddings) {
            processEmbedding(executionDetail, embedding);
        }
    }

    void processEmbedding(ZephyrExecutionSearchResponse.Execution executionDetail,
                                  Element.Step.Embedding embedding) {
        log.info("Processing embedding with mime type: {} for jira: {} using execution: {}",
            embedding.getMimeType(),
            executionDetail.getIssueKey(),
            executionDetail.getId());
        attachFileToExecution(executionDetail.getId(), embedding);
    }


    boolean hasEmbeddings(Element element) {
        return Util.hasItems(element.getSteps())
            && element.getSteps().stream()
            .anyMatch(step -> Util.hasItems(step.getEmbeddings()));
    }

    List<Element.Step.Embedding> getEmbeddings(Element element) {
        if (!Util.hasItems(element.getSteps())) {
            return new ArrayList<>();
        }
        return element.getSteps()
            .stream()
            .map(Element.Step::getEmbeddings)
            .filter(Util::hasItems)
            .flatMap(List::stream)
            .toList();
    }
}

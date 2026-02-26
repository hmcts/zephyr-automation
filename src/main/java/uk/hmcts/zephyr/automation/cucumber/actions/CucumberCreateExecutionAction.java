package uk.hmcts.zephyr.automation.cucumber.actions;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.actions.AbstractCreateExecutionAction;
import uk.hmcts.zephyr.automation.cucumber.CucumberTagService;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element;

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
        List<Element> tests = new ArrayList<>();
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            tests.addAll(cucumberFeature.getElements());
        }
        processTests(tests.stream()
            .filter(getElementFilter())
            .toList());
    }
}

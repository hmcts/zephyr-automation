package uk.hmcts.zephyr.automation.cucumber.actions;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.actions.AbstractUpdateTicketAction;
import uk.hmcts.zephyr.automation.cucumber.CucumberTagService;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element;

import java.util.List;

@Slf4j
public class CucumberUpdateTicketAction
    extends AbstractUpdateTicketAction<Element>
    implements CucumberAction, CucumberJiraDescription {

    public CucumberUpdateTicketAction() {
        super(new CucumberTagService());
    }

    @Override
    public void process() {
        log.info("Starting Cucumber Update Ticket Action");
        List<CucumberFeature> cucumberFeatures = getFeatures();
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            processFeature(cucumberFeature);
        }
    }

    private void processFeature(CucumberFeature cucumberFeature) {
        if (cucumberFeature.getElements() == null) {
            return;
        }
        cucumberFeature.getElements().stream()
            .filter(getElementFilter())
            .forEach(this::updateJiraIssue);
    }

    @Override
    public void jiraDescriptionPostProcess(Element test, StringBuilder builder) {
        CucumberJiraDescription.super.jiraDescriptionPostProcess(test, builder);
    }
}

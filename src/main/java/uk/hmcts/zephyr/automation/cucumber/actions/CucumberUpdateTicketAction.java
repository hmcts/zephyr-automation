package uk.hmcts.zephyr.automation.cucumber.actions;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.actions.AbstractUpdateTicketAction;
import uk.hmcts.zephyr.automation.cucumber.CucumberTagService;
import uk.hmcts.zephyr.automation.cucumber.models.Element;
import uk.hmcts.zephyr.automation.cucumber.models.Feature;

import java.util.List;

@Slf4j
public class CucumberUpdateTicketAction
    extends AbstractUpdateTicketAction<Element>
    implements CucumberAction {

    public CucumberUpdateTicketAction() {
        super(new CucumberTagService());
    }

    @Override
    public void process() {
        log.info("Starting Cucumber Update Ticket Action");
        List<Feature> features = getFeatures();
        for (Feature feature : features) {
            processFeature(feature);
        }
    }

    private void processFeature(Feature feature) {
        if (feature.getElements() == null) {
            return;
        }
        feature.getElements().stream()
            .filter(getElementFilter())
            .forEach(this::updateJiraIssue);
    }
}

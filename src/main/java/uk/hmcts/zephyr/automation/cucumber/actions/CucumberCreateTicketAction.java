package uk.hmcts.zephyr.automation.cucumber.actions;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.actions.AbstractCreateTicketAction;
import uk.hmcts.zephyr.automation.cucumber.CucumberTagService;
import uk.hmcts.zephyr.automation.cucumber.models.Element;
import uk.hmcts.zephyr.automation.cucumber.models.Feature;
import uk.hmcts.zephyr.automation.util.FileUtil;

import java.util.List;

@Slf4j
public class CucumberCreateTicketAction
    extends AbstractCreateTicketAction<Element>
    implements CucumberAction {

    public CucumberCreateTicketAction() {
        super(new CucumberTagService());
    }

    @Override
    public void process() {
        List<Feature> features = getFeatures();
        for (Feature feature : features) {
            processFeature(feature);
        }
        // Write the updated features back to the file
        FileUtil.writeToFile(Config.getReportPath(), features);
    }

    private void processFeature(Feature feature) {
        if (feature.getElements() == null) {
            return;
        }
        feature.getElements().stream()
            .filter(getElementFilter())
            .forEach(this::createJiraIssue);
    }
}

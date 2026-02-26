package uk.hmcts.zephyr.automation.cucumber.actions;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.actions.AbstractCreateTicketAction;
import uk.hmcts.zephyr.automation.cucumber.CucumberTagService;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element;
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
        List<CucumberFeature> cucumberFeatures = getFeatures();
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            processFeature(cucumberFeature);
        }
        // Write the updated features back to the file
        FileUtil.writeToFile(Config.getReportPath(), cucumberFeatures);
    }

    private void processFeature(CucumberFeature cucumberFeature) {
        if (cucumberFeature.getElements() == null) {
            return;
        }
        cucumberFeature.getElements().stream()
            .filter(getElementFilter())
            .forEach(this::createJiraIssue);
    }
}

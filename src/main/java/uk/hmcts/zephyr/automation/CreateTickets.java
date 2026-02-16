package uk.hmcts.zephyr.automation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.cucumber.report.Element;
import uk.hmcts.zephyr.automation.cucumber.report.Feature;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
public class CreateTickets {


    private final List<Feature> features;

    public void create() {
        for (Feature feature : features) {
            processFeature(feature);
        }
    }

    private void processFeature(Feature feature) {
        if (feature.getElements() == null) {
            return;
        }
        for (Element scenario : feature.getElements()) {
            processScenario(feature, scenario);
        }
    }

    private void processScenario(Feature feature, Element scenario) {
        if (scenario == null) {
            return;
        }
        Optional<String> name = TagUtil.extractJiraKeyFromTag(scenario);
        if (name.isPresent()) {
            log.info("Scenario '{}' in feature '{}' has JIRA key: {}", scenario.getName(), feature.getName(),
                name.get());
            return;//No need to create a ticket if it already has a JIRA key tag
        }
        log.info("Scenario '{}' in feature '{}' does not have a JIRA key tag", scenario.getName(), feature.getUri());

        //TODO create Jira Instance
        //TODO replace with actual key from created ticket
        String jiraKey = "PO-1234";
        TagUtil.addTag(feature, scenario, TagUtil.JIRA_KEY_TAG_PREFIX + jiraKey);
        //Add Jira key tag to scenario
    }
}

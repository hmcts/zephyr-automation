package uk.hmcts.zephyr.automation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.cucumber.report.Element;
import uk.hmcts.zephyr.automation.cucumber.report.Feature;
import uk.hmcts.zephyr.automation.jira.JiraConstants;
import uk.hmcts.zephyr.automation.jira.models.JiraIssue;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueFieldsWrapper;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueLink;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;
import uk.hmcts.zephyr.util.TagUtil;

import java.util.List;
import java.util.Optional;

import static uk.hmcts.zephyr.automation.Config.JIRA;

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

        JiraIssue jiraIssue = createJiraIssue(feature, scenario);

        addLinksToJiraIssue(jiraIssue, scenario);
        TagUtil.addTag(feature, scenario, TagUtil.JIRA_KEY_TAG_PREFIX + jiraIssue.getKey());
    }

    private JiraIssue createJiraIssue(Feature feature, Element scenario) {

        JiraIssueFieldsWrapper body = JiraIssueFieldsWrapper.builder()
            .fields(JiraIssueFieldsWrapper.Fields.builder()
                .project(JiraIssueFieldsWrapper.Project.builder().id(JiraConstants.PROJECT_ID).build())
                .summary(scenario.getName())
                .description(getJiraDescription(feature, scenario))
                .issuetype(JiraIssueFieldsWrapper.IssueType.builder().id(ZephyrConstants.ZEPHYR_ISSUE_TYPE_ID).build())
                .reporter(JiraIssueFieldsWrapper.Reporter.builder().name(JiraConstants.DEFAULT_REPORTER).build())
                .build())
            .build();

        //Add Epic link if there is one
        TagUtil.extractTagWithPrefix(scenario, TagUtil.JIRA_EPIC_TAG_PREFIX)
            .ifPresent(s -> body.getFields().setEpicLink(s));

        //Add components if there are any
        body.getFields().setComponents(
            TagUtil.extractTagListWithPrefix(scenario, TagUtil.JIRA_COMPONENT_TAG_PREFIX)
                .stream()
                .map(s -> {
                    String componentId = JIRA.getComponentByName(JiraConstants.PROJECT_ID, s).getId();
                    return (JiraIssueFieldsWrapper.Component) JiraIssueFieldsWrapper.Component.builder()
                        .id(componentId)
                        .build();
                })
                .toList());
        //Add labels if there are any
        body.getFields().setLabels(TagUtil.extractTagListWithPrefix(scenario, TagUtil.JIRA_LABEL_TAG_PREFIX));
        //Create the issue
        return JIRA.createIssue(body);
    }

    private void addLinksToJiraIssue(JiraIssue jiraIssue, Element scenario) {
        addLinksToJiraIssue(jiraIssue, scenario, TagUtil.JIRA_NFR_TAG_PREFIX, "Contributes");
        addLinksToJiraIssue(jiraIssue, scenario, TagUtil.JIRA_LINK_TAG_PREFIX, "Relates");
        addLinksToJiraIssue(jiraIssue, scenario, TagUtil.JIRA_STORY_TAG_PREFIX, "Relates");
        addLinksToJiraIssue(jiraIssue, scenario, TagUtil.JIRA_DEFECT_TAG_PREFIX, "Relates");
    }

    private void addLinksToJiraIssue(JiraIssue jiraIssue, Element scenario, String tagPrefix, String linkType) {
        TagUtil.extractTagListWithPrefix(scenario, tagPrefix)
            .forEach(key -> JIRA.linkIssue(createIssueLink(jiraIssue.getKey(), key, linkType)));
    }

    public JiraIssueLink createIssueLink(String sourceIssueKey, String destinationIssueKey, String linkType) {
        return JiraIssueLink.builder()
            .type(JiraIssueLink.Type.builder().name(linkType).build())
            .inwardIssue(JiraIssueLink.IssueReference.builder().key(destinationIssueKey).build())
            .outwardIssue(JiraIssueLink.IssueReference.builder().key(sourceIssueKey).build())
            .build();
    }

    private String getJiraDescription(Feature feature, Element scenario) {
        return new StringBuilder("Feature: [")
            .append(feature.getName())
            .append("|")
            .append(feature.getUri().replace("classpath:", Config.githubRepoBaseSrcDir + "/resources/") + "#L"
                + scenario.getLine())
            .append("]\r\nScenario: ")
            .append(scenario.getName())
            .toString();
    }
}

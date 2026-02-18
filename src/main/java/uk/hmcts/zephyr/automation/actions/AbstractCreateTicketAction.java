package uk.hmcts.zephyr.automation.actions;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.jira.JiraConfig;
import uk.hmcts.zephyr.automation.jira.models.JiraIssue;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueFieldsWrapper;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueLink;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;

import java.util.Optional;


@Getter
@Slf4j
public abstract class AbstractCreateTicketAction<T extends ZephyrTest>
    extends AbstractAction<T>
    implements CreateTicketAction {

    protected AbstractCreateTicketAction(TagService<T> tagService) {
        super(tagService);
        validateConfig();
    }

    private void validateConfig() {
        if (Config.getGithubRepoBaseSrcDir() == null) {
            throw new IllegalArgumentException(
                "For CREATE_TICKETS action type, github-repo-base-src-dir must be specified as a command line "
                    + "argument");
        }
        if (Config.getBasePath() == null) {
            throw new IllegalArgumentException(
                "For CREATE_TICKETS action type, base-path must be specified as a command line "
                    + "argument");
        }
        if (Config.getReportPath() == null) {
            throw new IllegalArgumentException(
                "For CREATE_EXECUTION action type, report-path must be specified as a command line "
                    + "argument");
        }
    }

    protected Optional<JiraIssue> createJiraIssue(T test) {
        Optional<String> name = getTagService().extractJiraKeyFromTag(test);
        if (name.isPresent()) {
            log.info("Test in {} has JIRA key: {}", test.getNameAndLocation(), name.get());
            return Optional.empty();
        }
        log.info("Test in {} does not have JIRA key tag", test.getNameAndLocation());

        JiraIssueFieldsWrapper body = JiraIssueFieldsWrapper.builder()
            .fields(JiraIssueFieldsWrapper.Fields.builder()
                .project(JiraIssueFieldsWrapper.Project.builder().id(JiraConfig.getProjectId()).build())
                .summary(test.getName())
                .description(getJiraDescription(test))
                .issuetype(JiraIssueFieldsWrapper.IssueType.builder().id(ZephyrConstants.ZEPHYR_ISSUE_TYPE_ID).build())
                .reporter(JiraIssueFieldsWrapper.Reporter.builder().name(JiraConfig.getDefaultUser()).build())
                .build())
            .build();

        //Add Epic link if there is one
        getTagService().extractTagWithPrefix(test, JiraConfig.JIRA_EPIC_TAG_PREFIX)
            .ifPresent(s -> body.getFields().setEpicLink(s));

        //Add components if there are any
        body.getFields().setComponents(
            getTagService().extractTagListWithPrefix(test, JiraConfig.JIRA_COMPONENT_TAG_PREFIX)
                .stream()
                .map(s -> {
                    String componentId = Config.getJira().getComponentByName(JiraConfig.getProjectId(), s).getId();
                    return (JiraIssueFieldsWrapper.Component) JiraIssueFieldsWrapper.Component.builder()
                        .id(componentId)
                        .build();
                })
                .toList());
        //Add labels if there are any
        body.getFields()
            .setLabels(getTagService().extractTagListWithPrefix(test, JiraConfig.JIRA_LABEL_TAG_PREFIX));

        //Create the issue
        JiraIssue jiraIssue = Config.getJira().createIssue(body);
        addLinksToJiraIssue(jiraIssue, test);
        getTagService().addTag(test, JiraConfig.JIRA_KEY_TAG_PREFIX + jiraIssue.getKey());
        return Optional.of(jiraIssue);
    }

    private void addLinksToJiraIssue(JiraIssue jiraIssue, T test) {
        addLinksToJiraIssue(jiraIssue, test, JiraConfig.JIRA_NFR_TAG_PREFIX, "Contributes");
        addLinksToJiraIssue(jiraIssue, test, JiraConfig.JIRA_LINK_TAG_PREFIX, "Relates");
        addLinksToJiraIssue(jiraIssue, test, JiraConfig.JIRA_STORY_TAG_PREFIX, "Relates");
        addLinksToJiraIssue(jiraIssue, test, JiraConfig.JIRA_DEFECT_TAG_PREFIX, "Relates");
    }

    private void addLinksToJiraIssue(JiraIssue jiraIssue, T test, String tagPrefix, String linkType) {
        getTagService().extractTagListWithPrefix(test, tagPrefix)
            .forEach(key -> Config.getJira().linkIssue(createIssueLink(jiraIssue.getKey(), key, linkType)));
    }

    public JiraIssueLink createIssueLink(String sourceIssueKey, String destinationIssueKey, String linkType) {
        return JiraIssueLink.builder()
            .type(JiraIssueLink.Type.builder().name(linkType).build())
            .inwardIssue(JiraIssueLink.IssueReference.builder().key(destinationIssueKey).build())
            .outwardIssue(JiraIssueLink.IssueReference.builder().key(sourceIssueKey).build())
            .build();
    }

    private String getJiraDescription(T test) {
        return new StringBuilder("Location: [")
            .append(test.getLocationDisplayName())
            .append("|")
            .append(test.getGitHubLink())
            .append("]\r\nScenario: ")
            .append(test.getName())
            .toString();
    }
}

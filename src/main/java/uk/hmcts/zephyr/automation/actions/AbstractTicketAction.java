package uk.hmcts.zephyr.automation.actions;

import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.jira.JiraConfig;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueFieldsWrapper;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueLink;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;

import java.util.List;
import java.util.Optional;

public abstract class AbstractTicketAction<T extends ZephyrTest> extends AbstractAction<T> {
    protected AbstractTicketAction(TagService<T> tagService) {
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

    protected void addLinksToJiraIssue(String sourceIssueKey, T test) {
        addLinksToJiraIssue(sourceIssueKey, test, JiraConfig.JIRA_NFR_TAG_PREFIX, "Contributes");
        addLinksToJiraIssue(sourceIssueKey, test, JiraConfig.JIRA_LINK_TAG_PREFIX, "Relates");
        addLinksToJiraIssue(sourceIssueKey, test, JiraConfig.JIRA_STORY_TAG_PREFIX, "Relates");
        addLinksToJiraIssue(sourceIssueKey, test, JiraConfig.JIRA_DEFECT_TAG_PREFIX, "Relates");
    }

    protected void addLinksToJiraIssue(String sourceIssueKey, T test, String tagPrefix, String linkType) {
        getTagService().extractTagListWithPrefix(test, tagPrefix)
            .forEach(destinationIssueKey -> Config.getJira().linkIssue(createIssueLink(sourceIssueKey, destinationIssueKey, linkType)));
    }

    protected JiraIssueLink createIssueLink(String sourceIssueKey, String destinationIssueKey, String linkType) {
        return JiraIssueLink.builder()
            .type(JiraIssueLink.Type.builder().name(linkType).build())
            .inwardIssue(JiraIssueLink.IssueReference.builder().key(destinationIssueKey).build())
            .outwardIssue(JiraIssueLink.IssueReference.builder().key(sourceIssueKey).build())
            .build();
    }

    protected String getJiraDescription(T test) {
        return new StringBuilder("Location: [")
            .append(test.getLocationDisplayName())
            .append("|")
            .append(test.getGitHubLink())
            .append("]\r\nScenario: ")
            .append(test.getName())
            .toString();
    }

    protected List<String> getLabels(T test) {
        return getTagService().extractTagListWithPrefix(test, JiraConfig.JIRA_LABEL_TAG_PREFIX);
    }

    protected List<JiraIssueFieldsWrapper.Component> getComponents(T test) {
        return getTagService().extractTagListWithPrefix(test, JiraConfig.JIRA_COMPONENT_TAG_PREFIX)
            .stream()
            .map(s -> {
                String componentId = Config.getJira().getComponentByName(JiraConfig.getProjectId(), s).getId();
                return (JiraIssueFieldsWrapper.Component) JiraIssueFieldsWrapper.Component.builder()
                    .id(componentId)
                    .build();
            })
            .toList();
    }

    protected Optional<String> getEpicTicketKey(T test) {
        return getTagService().extractTagWithPrefix(test, JiraConfig.JIRA_EPIC_TAG_PREFIX);
    }

    protected JiraIssueFieldsWrapper buildBody(T test, boolean isCreate) {
        JiraIssueFieldsWrapper body = JiraIssueFieldsWrapper.builder()
            .fields(JiraIssueFieldsWrapper.Fields.builder()
                .summary(test.getName())
                .description(getJiraDescription(test))
                .build())
            .build();

        if (isCreate) {
            JiraIssueFieldsWrapper.Fields fields = body.getFields();
            fields.setProject(new JiraIssueFieldsWrapper.Project(JiraConfig.getProjectId()));
            fields.setIssuetype(new JiraIssueFieldsWrapper.IssueType(ZephyrConstants.ZEPHYR_ISSUE_TYPE_ID));
            fields.setReporter(new JiraIssueFieldsWrapper.Reporter(JiraConfig.getDefaultUser()));
        }

        //Add Epic link if there is one
        getEpicTicketKey(test).ifPresent(s -> body.getFields().setEpicLink(s));
        //Add components if there are any
        body.getFields().setComponents(getComponents(test));
        //Add labels if there are any
        body.getFields().setLabels(getLabels(test));

        return body;
    }
}

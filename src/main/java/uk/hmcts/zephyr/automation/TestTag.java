package uk.hmcts.zephyr.automation;

public record TestTag(TestTag.Type type, String value) {

    public enum Type {
        JIRA_KEY,
        JIRA_COMPONENT,
        JIRA_LABEL,
        JIRA_EPIC,
        JIRA_NFR,
        JIRA_LINK,
        JIRA_STORY,
        JIRA_DEFECT,
        JIRA_IGNORE
    }
}

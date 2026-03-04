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

    public interface StandardTagPrefixProvider {
        default String getTagPrefix(TestTag.Type tagType) {
            return switch (tagType) {
                case JIRA_KEY -> "@JIRA-KEY:";
                case JIRA_COMPONENT -> "@JIRA-COMPONENT:";
                case JIRA_LABEL -> "@JIRA-LABEL:";
                case JIRA_EPIC -> "@JIRA-EPIC:";
                case JIRA_NFR -> "@JIRA-NFR:";
                case JIRA_LINK -> "@JIRA-LINK:";
                case JIRA_STORY -> "@JIRA-STORY:";
                case JIRA_DEFECT -> "@JIRA-DEFECT:";
                case JIRA_IGNORE -> "@JIRA-IGNORE";
            };
        }
    }

}

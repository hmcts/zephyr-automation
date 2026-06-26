package uk.hmcts.zephyr.automation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TestTag {
    final TestTag.Type type;
    String value;

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

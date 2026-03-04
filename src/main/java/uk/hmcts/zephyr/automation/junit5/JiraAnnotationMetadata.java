package uk.hmcts.zephyr.automation.junit5;

import java.util.List;

public record JiraAnnotationMetadata(
    List<String> jiraKey,
    List<String> jiraComponents,
    List<String> jiraLabels,
    List<String> jiraEpics,
    List<String> jiraNfrs,
    List<String> jiraLinks,
    List<String> jiraStories,
    List<String> jiraDefects,
    boolean jiraIgnore
) {
    public static JiraAnnotationMetadata empty() {
        return new JiraAnnotationMetadata(
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            false
        );
    }
}


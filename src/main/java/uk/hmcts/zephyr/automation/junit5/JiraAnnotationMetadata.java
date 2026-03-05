package uk.hmcts.zephyr.automation.junit5;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class JiraAnnotationMetadata {
    private Set<String> jiraKey;
    private Set<String> jiraComponents;
    private Set<String> jiraLabels;
    private Set<String> jiraEpics;
    private Set<String> jiraNfrs;
    private Set<String> jiraLinks;
    private Set<String> jiraStories;
    private Set<String> jiraDefects;
    private boolean jiraIgnore;

    public static JiraAnnotationMetadata empty() {
        return new JiraAnnotationMetadata(
            new HashSet<>(),
            new HashSet<>(),
            new HashSet<>(),
            new HashSet<>(),
            new HashSet<>(),
            new HashSet<>(),
            new HashSet<>(),
            new HashSet<>(),
            false
        );
    }
}


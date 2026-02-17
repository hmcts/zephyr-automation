package uk.hmcts.zephyr.automation.jira.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssueLink {
    private Type type;
    private IssueReference inwardIssue;
    private IssueReference outwardIssue;

    @Getter
    @Setter
    @SuperBuilder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Type {
        private String name;
    }

    @Getter
    @Setter
    @SuperBuilder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueReference {
        private String key;
    }
}

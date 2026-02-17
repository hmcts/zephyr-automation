package uk.hmcts.zephyr.automation.jira.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraSearchResponse {
    private String expand;
    private Integer startAt;
    private Integer maxResults;
    private Integer total;
    private List<JiraIssueSummary> issues;
    private Object warningMessages;
    private Object names;
    private Object schema;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JiraIssueSummary {
        private String expand;
        private String id;
        private String self;
        private String key;
        private JiraIssueFields fields;
        private Object renderedFields;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JiraIssueFields {
        private String summary;
    }
}



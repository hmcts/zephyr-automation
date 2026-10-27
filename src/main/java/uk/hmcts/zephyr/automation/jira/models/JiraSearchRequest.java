package uk.hmcts.zephyr.automation.jira.models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JiraSearchRequest {
    private List<String> fields;
    private String jql;
    private Integer maxResults;
    private String nextPageToken;
    private String expand;
    private Boolean reconcileIssues;
    private Integer startAt;
    private Boolean validateQuery;
}

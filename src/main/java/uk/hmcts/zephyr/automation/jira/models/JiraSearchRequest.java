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
    private Integer startAt;
    private Boolean validateQuery;
}


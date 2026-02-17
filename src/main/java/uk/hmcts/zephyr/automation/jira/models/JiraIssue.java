package uk.hmcts.zephyr.automation.jira.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class JiraIssue {
    private String id;
    private String key;
    private String self;
}


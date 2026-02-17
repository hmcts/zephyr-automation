package uk.hmcts.zephyr.automation.zephyr.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ZephyrExecutionRequest {
    private String cycleId;
    private String issueId;
    private String projectId;
    private String versionId;
    private String assigneeType;
    private String assignee;
    private Integer folderId;
}


package uk.hmcts.zephyr.automation.zephyr.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZephyrExecutionDetail {
    private Long id;
    private Long orderId;
    private String executionStatus;
    private String executionWorkflowStatus;
    private String comment;
    private String htmlComment;
    private Long cycleId;
    private String cycleName;
    private Long versionId;
    private String versionName;
    private Long projectId;
    private String createdBy;
    private String createdByDisplay;
    private String createdByUserName;
    private String modifiedBy;
    private String createdOn;
    private Long createdOnVal;
    private String assignedTo;
    private String assignedToDisplay;
    private String assignedToUserName;
    private String assigneeType;
    private Long issueId;
    private String issueKey;
    private String summary;
    private String issueDescription;
    private String label;
    private String component;
    private String projectKey;
    private Boolean canViewIssue;
    private Boolean isIssueEstimateNil;
    private Boolean isExecutionWorkflowEnabled;
    private Boolean isTimeTrackingEnabled;
}


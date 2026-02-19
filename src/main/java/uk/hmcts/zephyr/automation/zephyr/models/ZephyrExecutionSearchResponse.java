package uk.hmcts.zephyr.automation.zephyr.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZephyrExecutionSearchResponse {
    private Map<String, Status> status;
    private List<Execution> executions;
    private String currentlySelectedExecutionId;
    private Integer recordsCount;
    private String totalExecutionEstimatedTime;
    private String totalExecutionLoggedTime;
    private Integer executionsToBeLogged;
    private Boolean isExecutionWorkflowEnabledForProject;
    private Boolean isTimeTrackingEnabled;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private Integer id;
        private String color;
        private String description;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Execution {
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
        private Integer executionDefectCount;
        private Integer stepDefectCount;
        private Integer totalDefectCount;
        private String customFields;
    }
}


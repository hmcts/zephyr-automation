package uk.hmcts.zephyr.automation.zephyr.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZephyrExecutionSearchResponse {
    private List<SearchObject> searchObjectList;
    private List<Object> summaryList;
    private Integer totalCount;
    private Integer currentOffset;
    private Integer maxAllowed;
    private Integer maxAllowedforSelect;
    private String sortBy;
    private String sortOrder;
    private Map<String, Status> executionStatus;
    private Map<String, Status> stepExecutionStatus;

    public List<Execution> getExecutions() {
        if (searchObjectList == null) {
            return List.of();
        }
        return searchObjectList.stream()
            .filter(searchObject -> searchObject.getExecution() != null)
            .map(this::toExecution)
            .toList();
    }

    public Map<String, Status> getStatus() {
        return executionStatus;
    }

    public void setStatus(Map<String, Status> status) {
        this.executionStatus = status;
    }

    public void setExecutions(List<Execution> executions) {
        if (executions == null) {
            this.searchObjectList = null;
            return;
        }
        this.searchObjectList = executions.stream()
            .map(this::toSearchObject)
            .toList();
    }

    private Execution toExecution(SearchObject searchObject) {
        Execution execution = searchObject.getExecution();
        execution.setIssueKey(searchObject.getIssueKey());
        execution.setIssueLabel(searchObject.getIssueLabel());
        execution.setComponent(searchObject.getComponent());
        execution.setIssueSummary(searchObject.getIssueSummary());
        execution.setIssueDescription(searchObject.getIssueDescription());
        execution.setProjectName(searchObject.getProjectName());
        execution.setVersionName(searchObject.getVersionName());
        execution.setPriority(searchObject.getPriority());
        execution.setProjectKey(searchObject.getProjectKey());
        execution.setViewIssuePermission(searchObject.getViewIssuePermission());
        execution.setExecutionWorkflowEnabled(searchObject.getExecutionWorkflowEnabled());
        return execution;
    }

    private SearchObject toSearchObject(Execution execution) {
        SearchObject searchObject = new SearchObject();
        searchObject.setExecution(execution);
        searchObject.setIssueKey(execution.getIssueKey());
        searchObject.setIssueLabel(execution.getIssueLabel());
        searchObject.setComponent(execution.getComponent());
        searchObject.setIssueSummary(execution.getIssueSummary());
        searchObject.setIssueDescription(execution.getIssueDescription());
        searchObject.setProjectName(execution.getProjectName());
        searchObject.setVersionName(execution.getVersionName());
        searchObject.setPriority(execution.getPriority());
        searchObject.setProjectKey(execution.getProjectKey());
        searchObject.setViewIssuePermission(execution.getViewIssuePermission());
        searchObject.setExecutionWorkflowEnabled(execution.getExecutionWorkflowEnabled());
        return searchObject;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchObject {
        private String warningMessage;
        private String originMessage;
        private Execution execution;
        private String issueKey;
        private String issueLabel;
        private String component;
        private String issueSummary;
        private String issueDescription;
        private String projectName;
        private String versionName;
        private String priority;
        private String priorityIconUrl;
        private String executedByDisplayName;
        private String assigneeType;
        private String assignedToDisplayName;
        private List<Object> testStepBeans;
        private String defectsAsString;
        private String projectKey;
        private String plannedExecutionTimeFormatted;
        private String actualExecutionTimeFormatted;
        private String executionWorkflowStatus;
        private String workflowLoggedTimedIncreasePercentage;
        private String workflowCompletePercentage;
        private Boolean versionReleased;
        private String customFieldValuesAsString;
        private Boolean viewIssuePermission;
        private Boolean executionWorkflowEnabled;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        private String name;
        private Integer id;
        private String description;
        private String color;
        private Integer type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Execution {
        private String id;
        private Long issueId;
        private Integer versionId;
        private Integer projectId;
        private String cycleId;
        private Long orderId;
        private String executedBy;
        private Long executedOn;
        private String modifiedBy;
        private String createdBy;
        private String createdByAccountId;
        private Status status;
        private String cycleName;
        private List<Object> defects;
        private List<Object> stepDefects;
        private Integer executionDefectCount;
        private Integer stepDefectCount;
        private Integer totalDefectCount;
        private String tenantKey;
        private Long creationDate;
        private Boolean executedByZapi;
        private String zfjIndexType;
        private Integer issueTypeId;
        private String projectType;
        private Long issueIndex;
        private String projectCycleVersionIndex;
        private Integer executionStatusIndex;
        private String projectIssueCycleVersionIndex;
        private String issueKey;
        private String issueLabel;
        private String component;
        private String issueSummary;
        private String issueDescription;
        private String projectName;
        private String versionName;
        private String priority;
        private String projectKey;
        private Boolean viewIssuePermission;
        private Boolean executionWorkflowEnabled;
    }
}

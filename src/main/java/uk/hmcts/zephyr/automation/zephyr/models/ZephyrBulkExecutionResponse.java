package uk.hmcts.zephyr.automation.zephyr.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZephyrBulkExecutionResponse {
    private Boolean zipped;
    private String summaryMessage;
    private Integer totalSteps;
    private String errorMessage;
    private String entityId;
    private String message;
    private Integer completedSteps;
    private String timeTaken;
    private String stepMessage;
    private Double progress;
    private String id;
    private String entity;
    private List<String> stepMessages;
    private String stepLabel;

    public boolean isCompleted() {
        return progress != null && progress >= 1.0;
    }

    public boolean isFailed() {
        return errorMessage != null && !errorMessage.isEmpty();
    }
}


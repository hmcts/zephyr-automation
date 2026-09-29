package uk.hmcts.zephyr.automation.zephyr.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ZephyrExecutionStatusUpdateRequest {
    private List<String> executions;
    private Integer status;

    @JsonProperty("clearDefectMappingFlag")
    private boolean clearDefectMapping;

    @JsonProperty("testStepStatusChangeFlag")
    @Builder.Default
    private boolean testStepStatus = true;

    @Builder.Default
    private int stepStatus = -1;
}

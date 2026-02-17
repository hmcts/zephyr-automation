package uk.hmcts.zephyr.automation.zephyr.models;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ZephyrExecutionStatusUpdateRequest {
    private List<String> executions;
    private String status;
}


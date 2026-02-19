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
public class ZephyrBulkExecutionRequest {
    private List<String> issues;
    private String method;
    private String cycleId;
    private String projectId;
}


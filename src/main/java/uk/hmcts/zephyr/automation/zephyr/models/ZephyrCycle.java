package uk.hmcts.zephyr.automation.zephyr.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ZephyrCycle {
    private String clonedCycleId;
    private String name;
    private String build;
    private String environment;
    private String description;
    private String startDate;
    private String endDate;
    private String projectId;
    private String versionId;
    private Integer sprintId;
}


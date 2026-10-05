package uk.hmcts.zephyr.automation.zephyr;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ZephyrConstants {
    public static final String BASE_URL = "https://prod-api.zephyr4jiracloud.com/connect";
    public static final String ZEPHYR_ISSUE_TYPE_ID = "10005";


    @Getter
    @AllArgsConstructor
    public static enum ExecutionStatus {
        PASS(1),
        FAIL(2),
        WIP(3),
        BLOCKED(4),
        UNEXECUTED(-1);

        private final int statusId;
    }
}

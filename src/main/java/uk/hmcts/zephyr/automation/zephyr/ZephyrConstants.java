package uk.hmcts.zephyr.automation.zephyr;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ZephyrConstants {
    public static final String BASE_URL = "https://tools.hmcts.net/jira/rest/zapi/latest";
    public static final String ZEPHYR_ISSUE_TYPE_ID = "15601";


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

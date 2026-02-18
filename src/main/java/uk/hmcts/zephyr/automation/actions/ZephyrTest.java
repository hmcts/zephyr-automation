package uk.hmcts.zephyr.automation.actions;

import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;

public interface ZephyrTest {

    String getName();

    String getGitHubLink();

    String getNameAndLocation();

    ZephyrConstants.ExecutionStatus getZephyrExecutionStatus();

    String getLocationDisplayName();
}

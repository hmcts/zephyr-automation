package uk.hmcts.zephyr.automation.zephyr.client;

import feign.RequestLine;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycle;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycleResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionDetail;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionRequest;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionStatusUpdateRequest;

import java.util.Map;

public interface ZephyrClient {

    @RequestLine("POST /cycle")
    ZephyrCycleResponse createCycle(ZephyrCycle cycle);

    @RequestLine("POST /execution")
    Map<String, ZephyrExecutionDetail> createExecution(ZephyrExecutionRequest execution);

    @RequestLine("PUT /execution/updateBulkStatus")
    void updateExecutionStatus(ZephyrExecutionStatusUpdateRequest statusUpdateRequest);
}

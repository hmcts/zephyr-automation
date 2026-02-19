package uk.hmcts.zephyr.automation.zephyr.client;

import feign.Param;
import feign.RequestLine;
import uk.hmcts.zephyr.automation.zephyr.models.JobProgressToken;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrBulkExecutionRequest;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrBulkExecutionResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycle;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycleResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionDetail;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionRequest;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionSearchResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionStatusUpdateRequest;

import java.util.Map;

public interface ZephyrClient {

    @RequestLine("POST /cycle")
    ZephyrCycleResponse createCycle(ZephyrCycle cycle);

    @RequestLine("POST /execution")
    Map<String, ZephyrExecutionDetail> createExecution(ZephyrExecutionRequest execution);

    @RequestLine("POST /execution/addTestsToCycle")
    JobProgressToken addTestsToCycle(ZephyrBulkExecutionRequest bulkExecutionRequest);

    @RequestLine("GET /execution/jobProgress/{jobProgressToken}?type=add_tests_to_cycle_job_progress")
    ZephyrBulkExecutionResponse getAddTestsToCycleJobProgress(@Param("jobProgressToken") String jobProgressToken);

    @RequestLine("GET /execution?cycleId={cycleId}")
    ZephyrExecutionSearchResponse searchExecutions(@Param("cycleId") String cycleId);

    @RequestLine("PUT /execution/updateBulkStatus")
    void updateExecutionStatus(ZephyrExecutionStatusUpdateRequest statusUpdateRequest);
}

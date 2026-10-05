package uk.hmcts.zephyr.automation.zephyr.client;

import feign.Param;
import feign.RequestLine;
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

    @RequestLine("POST /public/rest/api/1.0/cycle")
    ZephyrCycleResponse createCycle(ZephyrCycle cycle);

    @RequestLine("POST /public/rest/api/1.0/execution")
    Map<String, ZephyrExecutionDetail> createExecution(ZephyrExecutionRequest execution);

    @RequestLine("POST /public/rest/api/1.0/executions/add/cycle/{cycleId}")
    String addTestsToCycle(@Param("cycleId") String cycleId, ZephyrBulkExecutionRequest bulkExecutionRequest);

    @RequestLine("GET /public/rest/api/1.0/jobprogress/{jobProgressToken}")
    ZephyrBulkExecutionResponse getAddTestsToCycleJobProgress(@Param("jobProgressToken") String jobProgressToken);

    @RequestLine("GET /public/rest/api/1.0/executions/search/cycle/{cycleId}?projectId={projectId}&versionId"
        + "={versionId}&size={size}")
    ZephyrExecutionSearchResponse searchExecutions(@Param("cycleId") String cycleId,
                                                   @Param("projectId") String projectId,
                                                   @Param("versionId")
                                                   String versionId,
                                                   @Param("size")
                                                   Integer size);

    @RequestLine("POST /public/rest/api/1.0/executions")
    String updateExecutionStatus(ZephyrExecutionStatusUpdateRequest statusUpdateRequest);
}

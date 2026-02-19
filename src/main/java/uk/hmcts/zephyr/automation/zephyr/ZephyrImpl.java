package uk.hmcts.zephyr.automation.zephyr;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.zephyr.client.Zephyr;
import uk.hmcts.zephyr.automation.zephyr.client.ZephyrClient;
import uk.hmcts.zephyr.automation.zephyr.models.JobProgressToken;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrBulkExecutionRequest;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrBulkExecutionResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycle;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycleResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionDetail;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionRequest;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionSearchResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionStatusUpdateRequest;
import uk.hmcts.zephyr.automation.util.Util;

import java.util.Map;

@Slf4j
public class ZephyrImpl implements Zephyr {
    private final ZephyrClient zephyrClient;

    public ZephyrImpl(ObjectMapper objectMapper, String baseUrl, String authToken) {
        zephyrClient = Feign.builder()
            .requestInterceptor(template -> {
                template.header("Authorization", authToken);
                template.header("Content-Type", "application/json");
            })
            .encoder(new JacksonEncoder(objectMapper))
            .decoder(new JacksonDecoder(objectMapper))
            .logLevel(Logger.Level.FULL)
            .logger(new Slf4jLogger())
            .target(ZephyrClient.class, baseUrl);
    }

    @Override
    public ZephyrCycleResponse createCycle(ZephyrCycle cycle) {
        log.info("Executing Zephyr create cycle: {}", Util.writeObjectToString(cycle));
        ZephyrCycleResponse response = zephyrClient.createCycle(cycle);
        log.info("Created Zephyr Cycle with ID: {}", response.getId());
        return response;
    }

    @Override
    public Map<String, ZephyrExecutionDetail> createExecution(ZephyrExecutionRequest execution) {
        log.info("Executing Zephyr Execution Request: {}", Util.writeObjectToString(execution));
        Map<String, ZephyrExecutionDetail> response = zephyrClient.createExecution(execution);
        log.info("Created Zephyr Execution with details: {}", response);
        return response;
    }

    @Override
    public void updateExecutionStatus(ZephyrExecutionStatusUpdateRequest statusUpdateRequest) {
        log.info("Executing Zephyr Update Executions status Request: {}",
            Util.writeObjectToString(statusUpdateRequest));
        zephyrClient.updateExecutionStatus(statusUpdateRequest);
    }

    //Passthrough
    @Override
    public JobProgressToken addTestsToCycle(ZephyrBulkExecutionRequest bulkExecutionRequest) {
        return zephyrClient.addTestsToCycle(bulkExecutionRequest);
    }

    @Override
    public ZephyrBulkExecutionResponse getAddTestsToCycleJobProgress(String jobProgressToken) {
        return zephyrClient.getAddTestsToCycleJobProgress(jobProgressToken);
    }

    @Override
    public ZephyrExecutionSearchResponse searchExecutions(String cycleId) {
        return zephyrClient.searchExecutions(cycleId);
    }

}

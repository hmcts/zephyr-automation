package uk.hmcts.zephyr.automation.zephyr;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.form.FormData;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.zephyr.client.Zephyr;
import uk.hmcts.zephyr.automation.zephyr.client.ZephyrClient;
import uk.hmcts.zephyr.automation.zephyr.client.ZephyrFormClient;
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

@Slf4j
public class ZephyrImpl implements Zephyr {
    private final ZephyrClient zephyrClient;
    private final ZephyrFormClient zephyrFormClient;

    public ZephyrImpl(ObjectMapper objectMapper, String baseUrl, String authToken) {
        zephyrClient = Feign.builder()
            .requestInterceptor(template -> {
                template.header("Authorization", authToken);
                template.header("Content-Type", "application/json");
            })
            .encoder(new JacksonEncoder(objectMapper))
            .decoder(new JacksonDecoder(objectMapper))
            .target(ZephyrClient.class, baseUrl);

        zephyrFormClient = Feign.builder()
            .requestInterceptor(template -> {
                template.header("Authorization", authToken);
                template.header("X-Atlassian-Token", "no-check");
            })
            .encoder(new FormEncoder())
            .target(ZephyrFormClient.class, baseUrl);
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

    @Override
    public ZephyrCycleResponse createCycle(ZephyrCycle cycle) {
        return zephyrClient.createCycle(cycle);
    }

    @Override
    public Map<String, ZephyrExecutionDetail> createExecution(ZephyrExecutionRequest execution) {
        return zephyrClient.createExecution(execution);
    }

    @Override
    public void updateExecutionStatus(ZephyrExecutionStatusUpdateRequest statusUpdateRequest) {
        zephyrClient.updateExecutionStatus(statusUpdateRequest);
    }

    @Override
    public void attachEvidence(String entityType, Long entityId, FormData formData) {
        zephyrFormClient.attachEvidence(entityType, entityId, formData);
    }
}

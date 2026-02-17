package uk.hmcts.zephyr.automation.zephyr;

import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.jira.JiraConstants;
import uk.hmcts.zephyr.automation.zephyr.client.Zephyr;
import uk.hmcts.zephyr.automation.zephyr.client.ZephyrClient;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycle;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycleResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionDetail;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionRequest;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionStatusUpdateRequest;

import java.util.Map;

@Slf4j
public class ZephyrImpl implements Zephyr {
    private final ZephyrClient zephyrClient;

    public ZephyrImpl() {
        zephyrClient = Feign.builder()
            .requestInterceptor(template -> {
                template.header("Authorization", "Bearer " + JiraConstants.AUTH_TOKEN);
                template.header("Content-Type", "application/json");
            })
            .encoder(new JacksonEncoder())
            .decoder(new JacksonDecoder())
            .logLevel(Logger.Level.FULL)
            .logger(new Slf4jLogger())
            .target(ZephyrClient.class, ZephyrConstants.BASE_URL);
    }

    @Override
    public ZephyrCycleResponse createCycle(ZephyrCycle cycle) {
        ZephyrCycleResponse response = zephyrClient.createCycle(cycle);
        log.info("Created Zephyr Cycle with ID: {}", response.getId());
        return response;
    }

    @Override
    public Map<String, ZephyrExecutionDetail> createExecution(ZephyrExecutionRequest execution) {
        return zephyrClient.createExecution(execution);
    }

    @Override
    public void updateExecutionStatus(ZephyrExecutionStatusUpdateRequest statusUpdateRequest) {
        zephyrClient.updateExecutionStatus(statusUpdateRequest);
    }

}

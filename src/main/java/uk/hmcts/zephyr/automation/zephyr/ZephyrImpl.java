package uk.hmcts.zephyr.automation.zephyr;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Logger;
import feign.form.FormData;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.util.Util;
import uk.hmcts.zephyr.automation.zephyr.client.ZephyrAuthenticationInterceptor;
import uk.hmcts.zephyr.automation.zephyr.client.ZephyrClient;
import uk.hmcts.zephyr.automation.zephyr.client.ZephyrDecoder;
import uk.hmcts.zephyr.automation.zephyr.client.ZephyrFormClient;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrBulkExecutionRequest;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrBulkExecutionResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycle;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycleResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionDetail;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionRequest;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionSearchResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionStatusUpdateRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ZephyrImpl {
    private static final int MAX_EXECUTION_SEARCH_PAGE_SIZE = 50;

    private final ZephyrClient zephyrClient;
    private final ZephyrFormClient zephyrFormClient;

    public ZephyrImpl(ObjectMapper objectMapper, String baseUrl, String accessKey, String secretKey, String accountId) {
        ZephyrAuthenticationInterceptor authenticationInterceptor =
            new ZephyrAuthenticationInterceptor(baseUrl, accessKey, secretKey, accountId);
        zephyrClient = Feign.builder()
            .requestInterceptor(authenticationInterceptor)
            .encoder(new JacksonEncoder(objectMapper))
            .decoder(new ZephyrDecoder(new JacksonDecoder(objectMapper)))
            .logLevel(Logger.Level.FULL)
            .logger(new Slf4jLogger())
            .target(ZephyrClient.class, baseUrl);

        zephyrFormClient = Feign.builder()
            .requestInterceptor(authenticationInterceptor)
            .encoder(new FormEncoder())
            .logLevel(Logger.Level.FULL)
            .logger(new Slf4jLogger())
            .target(ZephyrFormClient.class, baseUrl);
    }


    //Passthrough

    public ZephyrBulkExecutionResponse getAddTestsToCycleJobProgress(String jobProgressToken) {
        return zephyrClient.getAddTestsToCycleJobProgress(jobProgressToken);
    }

    public ZephyrExecutionSearchResponse searchExecutions(String cycleId,
                                                          String projectId,
                                                          String versionId,
                                                          Integer size) {
        int requestedSize = size == null ? MAX_EXECUTION_SEARCH_PAGE_SIZE : size;
        int pageSize = Math.min(requestedSize, MAX_EXECUTION_SEARCH_PAGE_SIZE);
        ZephyrExecutionSearchResponse response =
            zephyrClient.searchExecutions(cycleId, projectId, versionId, pageSize, 0);
        ZephyrExecutionSearchResponse page = response;
        List<ZephyrExecutionSearchResponse.SearchObject> searchObjects = new ArrayList<>();
        int offset = 0;
        while (page.getSearchObjectList() != null && !page.getSearchObjectList().isEmpty()) {
            int resultCount = page.getSearchObjectList().size();
            searchObjects.addAll(page.getSearchObjectList());
            offset += resultCount;
            if (offset >= requestedSize
                || (page.getTotalCount() != null && offset >= page.getTotalCount())
                || (page.getTotalCount() == null && resultCount < pageSize)) {
                break;
            }
            pageSize = Math.min(requestedSize - offset, MAX_EXECUTION_SEARCH_PAGE_SIZE);
            page = zephyrClient.searchExecutions(cycleId, projectId, versionId, pageSize, offset);
        }
        response.setSearchObjectList(searchObjects);
        return response;
    }

    public ZephyrCycleResponse createCycle(ZephyrCycle cycle) {
        System.out.println("TMP: " + Util.writeObjectToString(cycle));
        return zephyrClient.createCycle(cycle);
    }

    public Map<String, ZephyrExecutionDetail> createExecution(ZephyrExecutionRequest execution) {
        return zephyrClient.createExecution(execution);
    }

    @SneakyThrows
    public String addTestsToCycle(String cycleId, ZephyrBulkExecutionRequest bulkExecutionRequest) {
        return zephyrClient.addTestsToCycle(cycleId, bulkExecutionRequest);
    }

    @SneakyThrows
    public String updateExecutionStatus(ZephyrExecutionStatusUpdateRequest statusUpdateRequest) {
        return zephyrClient.updateExecutionStatus(statusUpdateRequest);
    }

    public void attachEvidence(String entityType, String entityId, FormData formData) {
        zephyrFormClient.attachEvidence(entityType, entityId, formData);
    }
}

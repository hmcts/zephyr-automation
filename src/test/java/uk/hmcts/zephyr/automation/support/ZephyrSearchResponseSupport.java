package uk.hmcts.zephyr.automation.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionSearchResponse;

import java.util.stream.IntStream;

public final class ZephyrSearchResponseSupport {

    private ZephyrSearchResponseSupport() {
    }

    public static String page(Integer totalCount, int offset, int count) throws JsonProcessingException {
        ZephyrExecutionSearchResponse response = new ZephyrExecutionSearchResponse();
        response.setTotalCount(totalCount);
        response.setCurrentOffset(offset);
        response.setExecutions(IntStream.range(offset, offset + count)
            .mapToObj(index -> {
                ZephyrExecutionSearchResponse.Execution execution = new ZephyrExecutionSearchResponse.Execution();
                execution.setId("execution-" + index);
                execution.setIssueKey("CASE-" + index);
                return execution;
            })
            .toList());
        return new ObjectMapper().writeValueAsString(response);
    }
}

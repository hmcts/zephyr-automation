package uk.hmcts.zephyr.automation.zephyr;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Client;
import feign.Feign;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import uk.hmcts.zephyr.automation.support.ZephyrSearchResponseSupport;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionSearchResponse;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ZephyrExecutionPaginationTest {

    @Nested
    class SearchExecutionsTest {
        @ParameterizedTest
        @CsvSource(value = {
            "1, 1, 1, 1:0:1",
            "49, 49, 49, 49:0:49",
            "50, 50, 50, 50:0:50",
            "51, 51, 51, 50:0:50|1:50:1",
            "200, 200, 200, 50:0:50|50:50:50|50:100:50|50:150:50",
            "201, 201, 201, 50:0:50|50:50:50|50:100:50|50:150:50|1:200:1",
            "200, 73, 73, 50:0:50|50:50:23",
            "200, 0, 0, 50:0:0",
            "50, 200, 50, 50:0:50",
            "200, NULL, 73, 50:0:50|50:50:23",
            "200, NULL, 50, 50:0:50|50:50:0",
            "200, 200, 50, 50:0:50|50:50:0",
            "NULL, 200, 50, 50:0:50",
            "200, 73, 73, 50:0:20|50:20:50|50:70:3"
        }, nullValues = "NULL")
        void given_resultCount_when_searching_then_fetchesRequestedExecutionsInPagesOfAtMost50(
            Integer requestedSize, Integer totalCount, int expectedCount, String pageSpecifications) throws Exception {
            List<String> expectedQueries = new ArrayList<>();
            ArrayDeque<String> pages = new ArrayDeque<>();
            for (String specification : pageSpecifications.split("\\|")) {
                String[] values = specification.split(":");
                expectedQueries.add("projectId=10013&versionId=-1&size=" + values[0] + "&offset=" + values[1]);
                pages.add(ZephyrSearchResponseSupport.page(totalCount, Integer.parseInt(values[1]),
                    Integer.parseInt(values[2])));
            }

            Client client = mock(Client.class);
            when(client.execute(any(), any())).thenAnswer(invocation -> Response.builder()
                .status(200)
                .reason("OK")
                .request(invocation.getArgument(0))
                .body(pages.removeFirst(), StandardCharsets.UTF_8)
                .build());
            Feign.Builder builder = Feign.builder().client(client);
            Feign.Builder formBuilder = Feign.builder().client(client);

            try (MockedStatic<Feign> feign = mockStatic(Feign.class, CALLS_REAL_METHODS)) {
                feign.when(Feign::builder).thenReturn(builder, formBuilder);
                ZephyrImpl subject = new ZephyrImpl(new ObjectMapper(), "https://zephyr.local", "access-key",
                    "secret-key", "account-id");

                ZephyrExecutionSearchResponse result = subject.searchExecutions("cycle-1", "10013", "-1",
                    requestedSize);

                assertEquals(expectedCount, result.getExecutions().size());
                assertEquals(totalCount, result.getTotalCount());
                if (expectedCount > 0) {
                    assertEquals("execution-0", result.getExecutions().getFirst().getId());
                    assertEquals("CASE-" + (expectedCount - 1), result.getExecutions().getLast().getIssueKey());
                }
                ArgumentCaptor<Request> requests = ArgumentCaptor.forClass(Request.class);
                verify(client, times(expectedQueries.size())).execute(requests.capture(), any());
                assertEquals(expectedQueries, requests.getAllValues().stream()
                    .map(request -> request.url().substring(request.url().indexOf('?') + 1))
                    .toList());
            }
        }
    }
}

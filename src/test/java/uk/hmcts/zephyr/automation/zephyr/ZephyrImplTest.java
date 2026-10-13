package uk.hmcts.zephyr.automation.zephyr;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.form.FormEncoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZephyrImplTest {

    private static final String BASE_URL = "https://zephyr.local";
    private static final String ACCESS_KEY = "access-key";
    private static final String SECRET_KEY = "secret-key";
    private static final String ACCOUNT_ID = "account-id";

    @Mock
    private Feign.Builder builder;

    @Mock
    private Feign.Builder formBuilder;

    @Mock
    private ZephyrClient zephyrClient;

    @Mock
    private ZephyrFormClient zephyrFormClient;

    private MockedStatic<Feign> feignStatic;

    private ZephyrImpl createSubject() {
        feignStatic = Mockito.mockStatic(Feign.class);
        feignStatic.when(Feign::builder).thenReturn(builder, formBuilder);

        lenient().when(builder.requestInterceptor(any())).thenReturn(builder);
        lenient().when(builder.encoder(any())).thenReturn(builder);
        lenient().when(builder.decoder(any())).thenReturn(builder);
        lenient().when(builder.logLevel(any())).thenReturn(builder);
        lenient().when(builder.logger(any())).thenReturn(builder);
        lenient().when(builder.target(eq(ZephyrClient.class), any())).thenReturn(zephyrClient);

        lenient().when(formBuilder.requestInterceptor(any())).thenReturn(formBuilder);
        lenient().when(formBuilder.encoder(any())).thenReturn(formBuilder);
        lenient().when(formBuilder.logLevel(any())).thenReturn(formBuilder);
        lenient().when(formBuilder.logger(any())).thenReturn(formBuilder);
        lenient().when(formBuilder.target(eq(ZephyrFormClient.class), any())).thenReturn(zephyrFormClient);

        return new ZephyrImpl(new ObjectMapper(), BASE_URL, ACCESS_KEY, SECRET_KEY, ACCOUNT_ID);
    }

    @AfterEach
    void tearDown() {
        if (feignStatic != null) {
            feignStatic.close();
            feignStatic = null;
        }
    }

    @Nested
    class ConstructorTest {
        @Test
        void given_validInputs_when_constructing_then_configuresFeignClients() {
            createSubject();

            verify(builder).requestInterceptor(any(RequestInterceptor.class));
            verify(builder).encoder(isA(JacksonEncoder.class));
            verify(builder).decoder(isA(ZephyrDecoder.class));
            verify(builder).logLevel(Logger.Level.FULL);
            verify(builder).logger(isA(Slf4jLogger.class));
            verify(builder).target(ZephyrClient.class, BASE_URL);

            verify(formBuilder).requestInterceptor(any(RequestInterceptor.class));
            verify(formBuilder).encoder(isA(FormEncoder.class));
            verify(formBuilder).logLevel(Logger.Level.FULL);
            verify(formBuilder).logger(isA(Slf4jLogger.class));
            verify(formBuilder).target(ZephyrFormClient.class, BASE_URL);
        }

        @Test
        void given_authorizationToken_when_interceptorInvoked_then_requiredHeadersAreApplied() {
            createSubject();

            ArgumentCaptor<RequestInterceptor> jsonInterceptorCaptor =
                ArgumentCaptor.forClass(RequestInterceptor.class);
            verify(builder).requestInterceptor(jsonInterceptorCaptor.capture());

            RequestTemplate jsonTemplate = new RequestTemplate();
            jsonTemplate.method("GET");
            jsonTemplate.target(BASE_URL);
            jsonTemplate.uri("/public/rest/api/1.0/cycles/search?projectId=10013&versionId=-1");
            jsonInterceptorCaptor.getValue().apply(jsonTemplate);

            Collection<String> authorizationHeader = jsonTemplate.headers().get("Authorization");
            Collection<String> contentTypeHeader = jsonTemplate.headers().get("Content-Type");

            assertEquals(1, authorizationHeader.size());
            assertEquals(List.of("application/json"), new ArrayList<>(contentTypeHeader));

            ArgumentCaptor<RequestInterceptor> formInterceptorCaptor =
                ArgumentCaptor.forClass(RequestInterceptor.class);
            verify(formBuilder).requestInterceptor(formInterceptorCaptor.capture());

            RequestTemplate formTemplate = new RequestTemplate();
            formTemplate.method("POST");
            formTemplate.target(BASE_URL);
            formTemplate.uri("/attachment?entityType=execution&entityId=1");
            formTemplate.header("Content-Type", "multipart/form-data");
            formInterceptorCaptor.getValue().apply(formTemplate);

            Collection<String> formAuthorization = formTemplate.headers().get("Authorization");
            Collection<String> xsrfHeader = formTemplate.headers().get("X-Atlassian-Token");

            assertEquals(1, formAuthorization.size());
            assertEquals(List.of("no-check"), new ArrayList<>(xsrfHeader));
        }
    }

    @Nested
    class AddTestsToCycleTest {
        @Test
        void given_bulkRequest_when_addTestsToCycle_then_delegatesToClient() {
            ZephyrImpl subject = createSubject();
            String cycleId = "cycle-1";
            ZephyrBulkExecutionRequest request = mock(ZephyrBulkExecutionRequest.class);
            String expectedResponse = "job-token";
            when(zephyrClient.addTestsToCycle(cycleId, request)).thenReturn(expectedResponse);

            String actualResponse = subject.addTestsToCycle(cycleId, request);

            assertSame(expectedResponse, actualResponse);
            verify(zephyrClient).addTestsToCycle(cycleId, request);
        }
    }

    @Nested
    class GetAddTestsToCycleJobProgressTest {
        @Test
        void given_jobToken_when_gettingProgress_then_delegatesToClient() {
            ZephyrImpl subject = createSubject();
            String jobToken = "job123";
            ZephyrBulkExecutionResponse expectedResponse = mock(ZephyrBulkExecutionResponse.class);
            when(zephyrClient.getAddTestsToCycleJobProgress(jobToken)).thenReturn(expectedResponse);

            ZephyrBulkExecutionResponse actualResponse = subject.getAddTestsToCycleJobProgress(jobToken);

            assertSame(expectedResponse, actualResponse);
            verify(zephyrClient).getAddTestsToCycleJobProgress(jobToken);
        }
    }

    @Nested
    class SearchExecutionsTest {
        @Test
        void given_cycleId_when_searching_then_delegatesToClient() {
            ZephyrImpl subject = createSubject();
            String cycleId = "cycle-1";
            String projectId = "10013";
            String versionId = "-1";
            Integer size = 50;
            ZephyrExecutionSearchResponse expectedResponse = mock(ZephyrExecutionSearchResponse.class);
            when(zephyrClient.searchExecutions(cycleId, projectId, versionId, size)).thenReturn(expectedResponse);

            ZephyrExecutionSearchResponse actualResponse =
                subject.searchExecutions(cycleId, projectId, versionId, size);

            assertSame(expectedResponse, actualResponse);
            verify(zephyrClient).searchExecutions(cycleId, projectId, versionId, size);
        }
    }

    @Nested
    class CreateCycleTest {
        @Test
        void given_cycle_when_creating_then_delegatesToClient() {
            ZephyrImpl subject = createSubject();
            ZephyrCycle cycle = ZephyrCycle.builder()
                .name("Regression")
                .projectId("10013")
                .versionId("-1")
                .build();
            ZephyrCycleResponse expectedResponse = new ZephyrCycleResponse();
            expectedResponse.setId("cycle-1");
            when(zephyrClient.createCycle(cycle)).thenReturn(expectedResponse);

            ZephyrCycleResponse actualResponse = subject.createCycle(cycle);

            assertSame(expectedResponse, actualResponse);
            verify(zephyrClient).createCycle(cycle);
        }
    }

    @Nested
    class CreateExecutionTest {
        @Test
        void given_executionRequest_when_creating_then_delegatesToClient() {
            ZephyrImpl subject = createSubject();
            ZephyrExecutionRequest request = mock(ZephyrExecutionRequest.class);
            Map<String, ZephyrExecutionDetail> expectedResponse = Map.of();
            when(zephyrClient.createExecution(request)).thenReturn(expectedResponse);

            Map<String, ZephyrExecutionDetail> actualResponse = subject.createExecution(request);

            assertSame(expectedResponse, actualResponse);
            verify(zephyrClient).createExecution(request);
        }
    }

    @Nested
    class UpdateExecutionStatusTest {
        @Test
        void given_statusUpdate_when_updating_then_delegatesToClient() {
            ZephyrImpl subject = createSubject();
            ZephyrExecutionStatusUpdateRequest request = mock(ZephyrExecutionStatusUpdateRequest.class);
            String expectedResponse = "job-token";
            when(zephyrClient.updateExecutionStatus(request)).thenReturn(expectedResponse);

            String actualResponse = subject.updateExecutionStatus(request);

            assertSame(expectedResponse, actualResponse);
            verify(zephyrClient).updateExecutionStatus(request);
        }
    }
}

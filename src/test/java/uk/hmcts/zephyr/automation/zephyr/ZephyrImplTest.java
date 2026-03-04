package uk.hmcts.zephyr.automation.zephyr;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
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
    private static final String AUTH_TOKEN = "Bearer token";

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
        lenient().when(builder.target(eq(ZephyrClient.class), any())).thenReturn(zephyrClient);

        lenient().when(formBuilder.requestInterceptor(any())).thenReturn(formBuilder);
        lenient().when(formBuilder.encoder(any())).thenReturn(formBuilder);
        lenient().when(formBuilder.target(eq(ZephyrFormClient.class), any())).thenReturn(zephyrFormClient);

        return new ZephyrImpl(new ObjectMapper(), BASE_URL, AUTH_TOKEN);
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
            verify(builder).decoder(isA(JacksonDecoder.class));
            verify(builder).target(ZephyrClient.class, BASE_URL);

            verify(formBuilder).requestInterceptor(any(RequestInterceptor.class));
            verify(formBuilder).encoder(isA(FormEncoder.class));
            verify(formBuilder).target(ZephyrFormClient.class, BASE_URL);
        }

        @Test
        void given_authorizationToken_when_interceptorInvoked_then_requiredHeadersAreApplied() {
            createSubject();

            ArgumentCaptor<RequestInterceptor> jsonInterceptorCaptor = ArgumentCaptor.forClass(RequestInterceptor.class);
            verify(builder).requestInterceptor(jsonInterceptorCaptor.capture());

            RequestTemplate jsonTemplate = new RequestTemplate();
            jsonInterceptorCaptor.getValue().apply(jsonTemplate);

            Collection<String> authorizationHeader = jsonTemplate.headers().get("Authorization");
            Collection<String> contentTypeHeader = jsonTemplate.headers().get("Content-Type");

            assertEquals(List.of(AUTH_TOKEN), new ArrayList<>(authorizationHeader));
            assertEquals(List.of("application/json"), new ArrayList<>(contentTypeHeader));

            ArgumentCaptor<RequestInterceptor> formInterceptorCaptor = ArgumentCaptor.forClass(RequestInterceptor.class);
            verify(formBuilder).requestInterceptor(formInterceptorCaptor.capture());

            RequestTemplate formTemplate = new RequestTemplate();
            formInterceptorCaptor.getValue().apply(formTemplate);

            Collection<String> formAuthorization = formTemplate.headers().get("Authorization");
            Collection<String> xsrfHeader = formTemplate.headers().get("X-Atlassian-Token");

            assertEquals(List.of(AUTH_TOKEN), new ArrayList<>(formAuthorization));
            assertEquals(List.of("no-check"), new ArrayList<>(xsrfHeader));
        }
    }

    @Nested
    class AddTestsToCycleTest {
        @Test
        void given_bulkRequest_when_addTestsToCycle_then_delegatesToClient() {
            ZephyrImpl subject = createSubject();
            ZephyrBulkExecutionRequest request = mock(ZephyrBulkExecutionRequest.class);
            JobProgressToken expectedResponse = JobProgressToken.builder().jobProgressToken("job-token").build();
            when(zephyrClient.addTestsToCycle(request)).thenReturn(expectedResponse);

            JobProgressToken actualResponse = subject.addTestsToCycle(request);

            assertSame(expectedResponse, actualResponse);
            verify(zephyrClient).addTestsToCycle(request);
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
            ZephyrExecutionSearchResponse expectedResponse = mock(ZephyrExecutionSearchResponse.class);
            when(zephyrClient.searchExecutions(cycleId)).thenReturn(expectedResponse);

            ZephyrExecutionSearchResponse actualResponse = subject.searchExecutions(cycleId);

            assertSame(expectedResponse, actualResponse);
            verify(zephyrClient).searchExecutions(cycleId);
        }
    }

    @Nested
    class CreateCycleTest {
        @Test
        void given_cycle_when_creating_then_delegatesToClient() {
            ZephyrImpl subject = createSubject();
            ZephyrCycle cycle = mock(ZephyrCycle.class);
            ZephyrCycleResponse expectedResponse = mock(ZephyrCycleResponse.class);
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

            subject.updateExecutionStatus(request);

            verify(zephyrClient).updateExecutionStatus(request);
        }
    }
}

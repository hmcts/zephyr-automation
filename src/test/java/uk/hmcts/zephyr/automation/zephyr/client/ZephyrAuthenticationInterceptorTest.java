package uk.hmcts.zephyr.automation.zephyr.client;

import com.thed.zephyr.cloud.rest.client.JwtGenerator;
import feign.RequestTemplate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ZephyrAuthenticationInterceptorTest {

    private static final String JWT_TOKEN = "JWT token";
    private static final String BASE_URL = "https://prod-api.zephyr4jiracloud.com/connect";
    private static final String ACCESS_KEY = "access-key";

    @Nested
    class ApplyTest {

        @Test
        void given_jsonRequest_when_applyingInterceptor_then_jwtAuthorizationAndJsonHeadersAreApplied() {
            JwtGenerator jwtGenerator = mock(JwtGenerator.class);
            when(jwtGenerator.generateJWT(
                "GET",
                URI.create(BASE_URL + "/public/rest/api/1.0/cycles/search?projectId=10013&versionId=-1"),
                360
            )).thenReturn(JWT_TOKEN);
            RequestTemplate template = new RequestTemplate();
            template.method("GET");
            template.target(BASE_URL);
            template.uri("/public/rest/api/1.0/cycles/search?projectId=10013&versionId=-1");

            new ZephyrAuthenticationInterceptor(jwtGenerator, ACCESS_KEY).apply(template);

            Collection<String> authorizationHeader = template.headers().get("Authorization");
            Collection<String> contentTypeHeader = template.headers().get("Content-Type");
            Collection<String> accessKeyHeader = template.headers().get("zapiAccessKey");

            assertEquals(List.of(JWT_TOKEN), new ArrayList<>(authorizationHeader));
            assertEquals(List.of("application/json"), new ArrayList<>(contentTypeHeader));
            assertEquals(List.of(ACCESS_KEY), new ArrayList<>(accessKeyHeader));
            verify(jwtGenerator).generateJWT(
                "GET",
                URI.create(BASE_URL + "/public/rest/api/1.0/cycles/search?projectId=10013&versionId=-1"),
                360
            );
        }

        @Test
        void given_multipartRequest_when_applyingInterceptor_then_authAndAtlassianHeadersAreApplied() {
            JwtGenerator jwtGenerator = mock(JwtGenerator.class);
            URI requestUri = URI.create(BASE_URL + "/attachment?entityType=execution&entityId=1");
            when(jwtGenerator.generateJWT("POST", requestUri, 360))
                .thenReturn(JWT_TOKEN);
            RequestTemplate template = new RequestTemplate();
            template.method("POST");
            template.target(BASE_URL);
            template.uri("/attachment?entityType=execution&entityId=1");
            template.header("Content-Type", "multipart/form-data");

            new ZephyrAuthenticationInterceptor(jwtGenerator, ACCESS_KEY).apply(template);

            Collection<String> authorizationHeader = template.headers().get("Authorization");
            Collection<String> contentTypeHeader = template.headers().get("Content-Type");
            Collection<String> atlassianHeader = template.headers().get("X-Atlassian-Token");
            final Collection<String> accessKeyHeader = template.headers().get("zapiAccessKey");

            assertEquals(List.of(JWT_TOKEN), new ArrayList<>(authorizationHeader));
            assertEquals(List.of("multipart/form-data"), new ArrayList<>(contentTypeHeader));
            assertEquals(List.of("no-check"), new ArrayList<>(atlassianHeader));
            assertEquals(List.of(ACCESS_KEY), new ArrayList<>(accessKeyHeader));
        }
    }
}

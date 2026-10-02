package uk.hmcts.zephyr.automation.zephyr.client;

import com.thed.zephyr.cloud.rest.ZFJCloudRestClient;
import com.thed.zephyr.cloud.rest.client.JwtGenerator;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.net.URI;
import java.util.Collection;

public class ZephyrAuthenticationInterceptor implements RequestInterceptor {
    private static final String ATLASSIAN_TOKEN = "X-Atlassian-Token";
    private static final String AUTHORIZATION = "Authorization";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String MULTIPART_CONTENT_TYPE = "multipart/form-data";
    private static final String NO_CHECK = "no-check";
    private static final String ZAPI_ACCESS_KEY = "zapiAccessKey";

    private static final int JWT_EXPIRATION_IN_SECONDS = 360;

    private final JwtGenerator jwtGenerator;
    private final String accessKey;

    public ZephyrAuthenticationInterceptor(String baseUrl, String accessKey, String secretKey, String accountId) {
        this(ZFJCloudRestClient.restBuilder(baseUrl, accessKey, secretKey, accountId).build().getJwtGenerator(),
            accessKey);
    }

    ZephyrAuthenticationInterceptor(JwtGenerator jwtGenerator, String accessKey) {
        this.jwtGenerator = jwtGenerator;
        this.accessKey = accessKey;
    }

    @Override
    public void apply(RequestTemplate template) {
        String jwt = jwtGenerator.generateJWT(template.method(), requestUri(template), JWT_EXPIRATION_IN_SECONDS);
        template.header(AUTHORIZATION, jwt);
        template.header(ZAPI_ACCESS_KEY, accessKey);
        if (isMultipart(template)) {
            template.header(ATLASSIAN_TOKEN, NO_CHECK);
        } else {
            template.header(CONTENT_TYPE, JSON_CONTENT_TYPE);
        }
    }

    private URI requestUri(RequestTemplate template) {
        String url = template.url();
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return URI.create(url);
        }
        return URI.create(template.feignTarget().url() + url);
    }

    private boolean isMultipart(RequestTemplate template) {
        Collection<String> contentTypes = template.headers().get(CONTENT_TYPE);
        return contentTypes != null && contentTypes.stream().anyMatch(this::isMultipart);
    }

    private boolean isMultipart(String contentType) {
        return contentType.toLowerCase().startsWith(MULTIPART_CONTENT_TYPE);
    }
}

package uk.hmcts.zephyr.automation.zephyr.atlassian.client.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.hmcts.zephyr.automation.zephyr.atlassian.model.ZConfig;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtGeneratorImplTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String ACCESS_KEY = "access-key";
    private static final String SECRET_KEY = "secret-key";
    private static final String ACCOUNT_ID = "account-id";
    private static final String BASE_URL = "https://prod-api.zephyr4jiracloud.com/connect";
    private static final int EXPIRY_SECONDS = 360;

    private final JwtGeneratorImpl subject =
        new JwtGeneratorImpl(new ZConfig(ACCESS_KEY, SECRET_KEY, ACCOUNT_ID, BASE_URL));

    @Nested
    class GenerateJwtTest {

        @Test
        void given_requestUri_when_generatingJwt_then_signedTokenContainsCanonicalRequestHash() throws Exception {
            URI uri = URI.create(BASE_URL + "/public/rest/api/1.0/cycles/search?versionId=-1&projectId=10013");

            String jwt = subject.generateJWT("GET", uri, EXPIRY_SECONDS);

            assertTrue(jwt.startsWith("JWT "));
            String token = jwt.substring("JWT ".length());
            String[] parts = token.split("\\.");
            assertEquals(3, parts.length);

            Map<String, Object> header = decodeJson(parts[0]);
            Map<String, Object> payload = decodeJson(parts[1]);

            assertEquals("JWT", header.get("typ"));
            assertEquals("HS256", header.get("alg"));
            assertEquals(ACCESS_KEY, payload.get("iss"));
            assertEquals(ACCOUNT_ID, payload.get("sub"));
            assertEquals(EXPIRY_SECONDS, ((Number) payload.get("exp")).longValue()
                - ((Number) payload.get("iat")).longValue());
            assertEquals(sha256Hex("GET&/public/rest/api/1.0/cycles/search&projectId=10013&versionId=-1"),
                payload.get("qsh"));
            assertEquals(sign(parts[0] + "." + parts[1]), parts[2]);
        }

        @Test
        void given_repeatedEncodedQueryParams_when_generatingJwt_then_qshSortsDecodedNamesAndValues() throws Exception {
            URI uri = URI.create(BASE_URL + "/attachment?entityId=2&entityType=execution&entityId=1&space=a%20b");

            String jwt = subject.generateJWT("POST", uri, EXPIRY_SECONDS);

            String payloadPart = jwt.substring("JWT ".length()).split("\\.")[1];
            Map<String, Object> payload = decodeJson(payloadPart);

            assertEquals(sha256Hex("POST&/attachment&entityId=1,2&entityType=execution&space=a%20b"),
                payload.get("qsh"));
        }

        @Test
        void given_jwtQueryParam_when_generatingJwt_then_qshIgnoresJwtParam() throws Exception {
            URI uri = URI.create(BASE_URL + "/attachment?entityId=1&jwt=existing-token&entityType=execution");

            String jwt = subject.generateJWT("POST", uri, EXPIRY_SECONDS);

            String payloadPart = jwt.substring("JWT ".length()).split("\\.")[1];
            Map<String, Object> payload = decodeJson(payloadPart);

            assertEquals(sha256Hex("POST&/attachment&entityId=1&entityType=execution"), payload.get("qsh"));
        }
    }

    private static Map<String, Object> decodeJson(String base64Url) throws Exception {
        byte[] json = Base64.getUrlDecoder().decode(base64Url);
        return OBJECT_MAPPER.readValue(json, new TypeReference<>() {
        });
    }

    private static String sign(String signingInput) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
    }

    private static String sha256Hex(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte currentByte : digest) {
            hex.append(String.format("%02x", currentByte));
        }
        return hex.toString();
    }
}

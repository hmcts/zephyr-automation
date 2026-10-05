package uk.hmcts.zephyr.automation.zephyr.atlassian.client.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.hmcts.zephyr.automation.zephyr.atlassian.client.JwtGenerator;
import uk.hmcts.zephyr.automation.zephyr.atlassian.model.ZConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class JwtGeneratorImpl implements JwtGenerator {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String JWT_AUTH_HEADER_PREFIX = "JWT ";
    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final String SHA_256 = "SHA-256";

    private final ZConfig config;
    private final Clock clock;

    public JwtGeneratorImpl(ZConfig config) {
        this(config, Clock.systemUTC());
    }

    JwtGeneratorImpl(ZConfig config, Clock clock) {
        this.config = config;
        this.clock = clock;
    }

    @Override
    public String generateJWT(String requestMethod, URI uri, int jwtExpiryWindowSeconds) {
        try {
            URI uriWithoutProductContext = getUri(uri, config.getZephyrBaseUrl());
            return JWT_AUTH_HEADER_PREFIX + createJwt(requestMethod, uriWithoutProductContext, jwtExpiryWindowSeconds);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Unable to create JWT for request URI " + uri, e);
        }
    }

    private URI getUri(URI uri, String baseUrlString) throws URISyntaxException {
        String path = uri.getPath();
        URI baseUrl = new URI(baseUrlString);
        String productContext = baseUrl.getPath();
        String pathWithoutProductContext = path.substring(productContext.length());
        return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
            pathWithoutProductContext, uri.getQuery(), uri.getFragment());
    }

    private String createJwt(String requestMethod, URI uri, int jwtExpiryWindowSeconds) {
        String header = base64UrlJson(Map.of("typ", "JWT", "alg", "HS256"));
        String payload = base64UrlJson(payload(requestMethod, uri, jwtExpiryWindowSeconds));
        String signingInput = header + "." + payload;
        return signingInput + "." + sign(signingInput);
    }

    private Map<String, Object> payload(String requestMethod, URI uri, int jwtExpiryWindowSeconds) {
        long issuedAt = clock.instant().getEpochSecond();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("iss", config.getAccessKey());
        payload.put("iat", issuedAt);
        payload.put("exp", issuedAt + jwtExpiryWindowSeconds);
        if (config.getAccountId() != null) {
            payload.put("sub", config.getAccountId());
        }
        payload.put("qsh", queryStringHash(requestMethod, uri));
        return payload;
    }

    private String queryStringHash(String requestMethod, URI uri) {
        return sha256Hex(requestMethod.toUpperCase() + "&" + canonicalPath(uri) + "&" + canonicalQuery(uri));
    }

    private String canonicalPath(URI uri) {
        String path = uri.getRawPath();
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path;
    }

    private String canonicalQuery(URI uri) {
        String query = uri.getRawQuery();
        if (query == null || query.isBlank()) {
            return "";
        }
        Map<String, ArrayList<String>> queryParams = new TreeMap<>();
        for (String pair : query.split("&")) {
            addQueryParam(queryParams, pair);
        }
        return queryParams.entrySet().stream()
            .map(entry -> canonicalParam(entry.getKey(), entry.getValue()))
            .reduce((left, right) -> left + "&" + right)
            .orElse("");
    }

    private void addQueryParam(Map<String, ArrayList<String>> queryParams, String pair) {
        int equalsIndex = pair.indexOf('=');
        String rawName = equalsIndex < 0 ? pair : pair.substring(0, equalsIndex);
        String name = urlDecode(rawName);
        if (name.isBlank() || "jwt".equals(name)) {
            return;
        }
        String rawValue = equalsIndex < 0 ? "" : pair.substring(equalsIndex + 1);
        queryParams.computeIfAbsent(name, ignored -> new ArrayList<>())
            .add(urlDecode(rawValue));
    }

    private String canonicalParam(String name, ArrayList<String> values) {
        values.sort(Comparator.naturalOrder());
        String encodedValues = values.stream()
            .map(this::urlEncode)
            .reduce((left, right) -> left + "," + right)
            .orElse("");
        return urlEncode(name) + "=" + encodedValues;
    }

    private String base64UrlJson(Map<String, Object> value) {
        try {
            return base64Url(OBJECT_MAPPER.writeValueAsBytes(value));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize JWT content", e);
        }
    }

    private String sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(new SecretKeySpec(config.getSecretKey().getBytes(StandardCharsets.UTF_8), HMAC_SHA_256));
            return base64Url(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign JWT", e);
        }
    }

    private String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance(SHA_256).digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte currentByte : digest) {
                hex.append(String.format("%02x", currentByte));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash canonical request", e);
        }
    }

    private String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
            .replace("+", "%20")
            .replace("*", "%2A")
            .replace("%7E", "~");
    }

    private String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}

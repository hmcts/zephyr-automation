package uk.hmcts.zephyr.automation.zephyr.atlassian.client;

import java.net.URI;

/**
 * Created by aliakseimatsarski on 11/3/16.
 */
public interface JwtGenerator {

    String generateJWT(String requestMethod, URI uri, int jwtExpiryWindowSeconds);
}

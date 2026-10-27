package uk.hmcts.zephyr.automation.zephyr.atlassian;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.zephyr.atlassian.client.JwtGenerator;
import uk.hmcts.zephyr.automation.zephyr.atlassian.client.impl.JwtGeneratorImpl;
import uk.hmcts.zephyr.automation.zephyr.atlassian.model.ZConfig;


@Getter
@Slf4j
public class ZFJCloudRestClient {

    private JwtGenerator jwtGenerator;


    private ZFJCloudRestClient() {
    }

    public static Builder restBuilder(String zephyrBaseUrl, String accessKey, String secretKey, String accountId) {
        return new ZFJCloudRestClient().new Builder(zephyrBaseUrl, accessKey, secretKey, accountId);
    }

    public class Builder {

        private final String accessKey;
        private final String secretKey;
        private final String accountId;
        private final String zephyrBaseUrl;

        private Builder(String zephyrBaseUrl, String accessKey,
                        String secretKey, String accountId) {
            this.zephyrBaseUrl = zephyrBaseUrl;
            this.accessKey = accessKey;
            this.secretKey = secretKey;
            this.accountId = accountId;
        }

        public ZFJCloudRestClient build() {
            ZConfig zConfig = new ZConfig(accessKey, secretKey, accountId, zephyrBaseUrl);
            jwtGenerator = new JwtGeneratorImpl(zConfig);

            return ZFJCloudRestClient.this;
        }
    }
}

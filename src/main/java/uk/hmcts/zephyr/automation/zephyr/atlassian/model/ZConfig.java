package uk.hmcts.zephyr.automation.zephyr.atlassian.model;

public class ZConfig {

    private final String accessKey;
    private final String secretKey;
    private final String accountId;
    private final String zephyrBaseUrl;

    public ZConfig(String accessKey, String secretKey, String accountId, String zephyrBaseUrl) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.accountId = accountId;
        this.zephyrBaseUrl = zephyrBaseUrl;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getZephyrBaseUrl() {
        return zephyrBaseUrl;
    }
}

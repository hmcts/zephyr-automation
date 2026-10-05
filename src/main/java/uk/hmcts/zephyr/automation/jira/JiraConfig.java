package uk.hmcts.zephyr.automation.jira;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class JiraConfig {

    private static JiraConfig INSTANCE;

    private final String baseUrl;
    private final String projectId;
    private final String defaultUserId;
    private final String authUsername;
    private final String authToken;
    private final List<String> defaultComponents;

    public static void instantiate(String[] args) {
        if (INSTANCE != null) {
            throw new IllegalStateException("JiraConfig has already been instantiated");
        }
        INSTANCE = new JiraConfig(args);
    }

    private JiraConfig(String[] args) {
        String baseUrl = null;
        String projectId = null;
        String defaultUserId = null;

        String authUsername = null;
        String authToken = null;
        List<String> defaultComponents = new ArrayList<>();

        for (String arg : args) {
            if (arg.startsWith("jira-base-url=")) {
                baseUrl = arg.substring("jira-base-url=".length());
            } else if (arg.startsWith("jira-project-id=")) {
                projectId = arg.substring("jira-project-id=".length());
            } else if (arg.startsWith("jira-default-user-id=")) {
                defaultUserId = arg.substring("jira-default-user-id=".length());
            } else if (arg.startsWith("jira-auth-username=")) {
                authUsername = arg.substring("jira-auth-username=".length());
            } else if (arg.startsWith("jira-auth-token=")) {
                authToken = arg.substring("jira-auth-token=".length());
            } else if (arg.startsWith("jira-default-components=")) {
                String componentsStr = arg.substring("jira-default-components=".length());
                String[] components = componentsStr.split(",");
                Arrays.stream(components)
                    .map(String::trim)
                    .forEach(defaultComponents::add);
            }
        }

        if (baseUrl == null || projectId == null || defaultUserId == null || authToken == null
            || authUsername == null) {
            throw new IllegalArgumentException(
                "Jira configuration requires jira-base-url"
                    + ", jira-project-id"
                    + ", jira-default-user-id"
                    + ", jira-auth-token"
                    + ", jira-auth-username"
                    + " to be specified as command line arguments");
        }
        this.baseUrl = baseUrl;
        this.projectId = projectId;
        this.defaultUserId = defaultUserId;
        this.authUsername = authUsername;
        this.authToken = authToken;
        this.defaultComponents = Collections.unmodifiableList(defaultComponents);
    }


    public static String getBaseUrl() {
        return INSTANCE.baseUrl;
    }

    public static String getProjectId() {
        return INSTANCE.projectId;
    }

    public static String getDefaultUserId() {
        return INSTANCE.defaultUserId;
    }

    public static String getAuthUsername() {
        return INSTANCE.authUsername;
    }

    public static String getAuthToken() {
        return INSTANCE.authToken;
    }

    public static List<String> getDefaultComponents() {
        return INSTANCE.defaultComponents;
    }
}

package uk.hmcts.zephyr.automation.jira;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class JiraConfig {

    private static JiraConfig INSTANCE;
    public static final String JIRA_KEY_TAG_PREFIX = "JIRA-KEY:";
    public static final String JIRA_COMPONENT_TAG_PREFIX = "JIRA-COMPONENT:";
    public static final String JIRA_LABEL_TAG_PREFIX = "JIRA-LABEL:";
    public static final String JIRA_EPIC_TAG_PREFIX = "JIRA-EPIC:";
    public static final String JIRA_NFR_TAG_PREFIX = "JIRA-NFR:";
    public static final String JIRA_LINK_TAG_PREFIX = "JIRA-LINK:";
    public static final String JIRA_STORY_TAG_PREFIX = "JIRA-STORY:";
    public static final String JIRA_DEFECT_TAG_PREFIX = "JIRA-DEFECT:";

    public static final String JIRA_IGNORE = "JIRA-IGNORE";

    private final String baseUrl;
    private final String projectId;
    private final String defaultUser;
    private final String authToken;
    private final String epicLinkCustomFieldId;
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
        String defaultUser = null;
        String authToken = null;
        String epicLinkCustomFieldId = null;
        List<String> defaultComponents = new ArrayList<>();

        for (String arg : args) {
            if (arg.startsWith("jira-base-url=")) {
                baseUrl = arg.substring("jira-base-url=".length());
            } else if (arg.startsWith("jira-project-id=")) {
                projectId = arg.substring("jira-project-id=".length());
            } else if (arg.startsWith("jira-default-user=")) {
                defaultUser = arg.substring("jira-default-user=".length());
            } else if (arg.startsWith("jira-auth-token=")) {
                authToken = arg.substring("jira-auth-token=".length());
            } else if (arg.startsWith("jira-epic-link-custom-field-id=")) {
                epicLinkCustomFieldId = arg.substring("jira-epic-link-custom-field-id=".length());
            } else if (arg.startsWith("jira-default-components=")) {
                String componentsStr = arg.substring("jira-default-components=".length());
                String[] components = componentsStr.split(",");
                Arrays.stream(components)
                    .map(String::trim)
                    .forEach(defaultComponents::add);
            }
        }

        if (baseUrl == null || projectId == null || defaultUser == null || authToken == null) {
            throw new IllegalArgumentException(
                "Jira configuration requires jira-base-url, jira-project-id, jira-default-user, and jira-auth-token "
                    + "to be specified as command line arguments");
        }
        this.baseUrl = baseUrl;
        this.projectId = projectId;
        this.defaultUser = defaultUser;
        this.authToken = authToken;
        this.epicLinkCustomFieldId = epicLinkCustomFieldId;
        this.defaultComponents = Collections.unmodifiableList(defaultComponents);
    }


    public static String getBaseUrl() {
        return INSTANCE.baseUrl;
    }

    public static String getProjectId() {
        return INSTANCE.projectId;
    }

    public static String getDefaultUser() {
        return INSTANCE.defaultUser;
    }

    public static String getAuthToken() {
        return INSTANCE.authToken;
    }

    public static String getEpicLinkCustomFieldId() {
        return INSTANCE.epicLinkCustomFieldId;
    }

    public static List<String> getDefaultComponents() {
        return INSTANCE.defaultComponents;
    }
}

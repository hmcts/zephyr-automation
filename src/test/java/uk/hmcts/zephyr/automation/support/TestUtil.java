package uk.hmcts.zephyr.automation.support;

import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.jira.JiraConfig;

import java.lang.reflect.Field;

public class TestUtil {

    public static void setField(Class<?> clazz, Object target, String fieldName, Object value) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    public static void resetSingletons() throws Exception {
        Field configInstance = Config.class.getDeclaredField("INSTANCE");
        configInstance.setAccessible(true);
        configInstance.set(null, null);

        Field jiraInstance = JiraConfig.class.getDeclaredField("INSTANCE");
        jiraInstance.setAccessible(true);
        jiraInstance.set(null, null);
    }

    public static String[] defaultArgs(Config.ActionType actionType, Config.ProcessType processType) {
        return new String[] {
            "action-type=" + actionType.name(),
            "process-type=" + processType.name(),
            "base-path=/tmp/base",
            "report-path=/tmp/report.json",
            "github-repo-base-src-dir=/repo",
            "jira-base-url=https://jira.example",
            "jira-project-id=PROJ",
            "jira-default-user=bot@example.com",
            "jira-auth-token=token",
            "jira-epic-link-custom-field-id=custom_1",
            "jira-default-components=Default"
        };
    }

    public static String[] defaultArgs(
        Config.ActionType actionType,
        Config.ProcessType processType,
        String basePath
    ) {
        String[] args = defaultArgs(actionType, processType);
        args[2] = "base-path=" + basePath;
        return args;
    }
}

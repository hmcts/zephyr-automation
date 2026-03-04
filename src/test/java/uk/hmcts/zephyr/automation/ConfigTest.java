package uk.hmcts.zephyr.automation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.hmcts.zephyr.automation.actions.Action;
import uk.hmcts.zephyr.automation.jira.JiraConfig;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("LineLength")
class ConfigTest {

    @AfterEach
    void tearDown() throws Exception {
        resetSingletons();
    }

    @Test
    void instantiate_populatesConfigAndJiraConfig() {
        String[] args = new String[] {
            "action-type=CREATE_TICKETS",
            "process-type=CUCUMBER_JSON_REPORT",
            "base-path=/tmp/base",
            "report-path=/tmp/report.json",
            "github-repo-base-src-dir=/repo",
            "jira-base-url=https://jira.example",
            "jira-project-id=PROJ",
            "jira-default-user=bot@example.com",
            "jira-auth-token=token",
            "jira-epic-link-custom-field-id=custom_1",
            "jira-default-components=CompA,CompB",
            "execution-environment=stg",
            "execution-build=1.0.0",
            "execution-test-cycle-name=SomeTest cycle name",
            "execution-attach-evidence=true",
        };

        Config.instantiate(args);

        assertEquals(Config.ActionType.CREATE_TICKETS, Config.getActionType());
        assertEquals(Config.ProcessType.CUCUMBER_JSON_REPORT, Config.getProcessType());
        assertEquals("/tmp/base", Config.getBasePath());
        assertEquals("/tmp/report.json", Config.getReportPath());
        assertEquals("/repo", Config.getGithubRepoBaseSrcDir());
        assertEquals("stg", Config.getExecutionEnvironment());
        assertEquals("1.0.0", Config.getExecutionBuild());
        assertEquals("SomeTest cycle name", Config.getTestCycleName());
        assertTrue(Config.shouldAttachEvidence());
        assertNotNull(Config.getJira());
        assertNotNull(Config.getZephyr());
        assertNotNull(Config.getObjectMapper());
        assertEquals("https://jira.example", JiraConfig.getBaseUrl());
        assertEquals("PROJ", JiraConfig.getProjectId());
        assertEquals("bot@example.com", JiraConfig.getDefaultUser());
        assertEquals("token", JiraConfig.getAuthToken());
        assertEquals("custom_1", JiraConfig.getEpicLinkCustomFieldId());
        assertEquals(2, JiraConfig.getDefaultComponents().size());
    }

    @Test
    void instantiate_missingActionOrProcessTypeThrows() {
        String[] args = new String[] {"action-type=CREATE_TICKETS"};

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Config.instantiate(args));
        assertEquals("Both action-type and process-type must be specified as command line arguments", exception.getMessage());
    }

    @Test
    void instantiate_missingJiraArgsThrows()  {
        String[] args = new String[] {
            "action-type=CREATE_TICKETS",
            "process-type=CUCUMBER_JSON_REPORT"
        };

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Config.instantiate(args));
        assertEquals("Jira configuration requires jira-base-url, jira-project-id, jira-default-user, and jira-auth-token to be specified as command line arguments", exception.getMessage());
    }

    private void resetSingletons() throws Exception {
        Field configInstance = Config.class.getDeclaredField("INSTANCE");
        configInstance.setAccessible(true);
        configInstance.set(null, null);

        Field jiraInstance = JiraConfig.class.getDeclaredField("INSTANCE");
        jiraInstance.setAccessible(true);
        jiraInstance.set(null, null);
    }


    @Nested
    class ProcessTypeTest {
        @Test
        void processAction_usesSupplierFromMap() throws Exception {
            Config.ProcessType processType = Config.ProcessType.CUCUMBER_JSON_REPORT;
            Field field = Config.ProcessType.class.getDeclaredField("actionTypeSupplierMap");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Config.ActionType, Supplier<Action>> original =
                (Map<Config.ActionType, Supplier<Action>>) field.get(processType);

            AtomicBoolean ran = new AtomicBoolean(false);
            Map<Config.ActionType, Supplier<Action>> replacement = new EnumMap<>(Config.ActionType.class);
            replacement.put(Config.ActionType.CREATE_TICKETS, () -> () -> ran.set(true));

            field.set(processType, replacement);
            try {
                processType.processAction(Config.ActionType.CREATE_TICKETS);
                assertTrue(ran.get());
            } finally {
                field.set(processType, original);
            }
        }

        @Test
        void processAction_throwsForUnsupportedActionType() throws Exception {
            Config.ProcessType processType = Config.ProcessType.CUCUMBER_JSON_REPORT;
            Field field = Config.ProcessType.class.getDeclaredField("actionTypeSupplierMap");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Config.ActionType, Supplier<Action>> original =
                (Map<Config.ActionType, Supplier<Action>>) field.get(processType);

            Map<Config.ActionType, Supplier<Action>> replacement = new EnumMap<>(Config.ActionType.class);
            replacement.put(Config.ActionType.CREATE_TICKETS, () -> () -> { });

            field.set(processType, replacement);
            try {
                UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                    () -> processType.processAction(Config.ActionType.UPDATE_TICKETS));

                assertEquals("Unsupported action type: UPDATE_TICKETS for process type: CUCUMBER_JSON_REPORT",
                    exception.getMessage());
            } finally {
                field.set(processType, original);
            }
        }
    }
}

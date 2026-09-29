package uk.hmcts.zephyr.automation;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.actions.Action;
import uk.hmcts.zephyr.automation.cucumber.actions.CucumberCreateExecutionAction;
import uk.hmcts.zephyr.automation.cucumber.actions.CucumberCreateTicketAction;
import uk.hmcts.zephyr.automation.cucumber.actions.CucumberUpdateTicketAction;
import uk.hmcts.zephyr.automation.cypress.actions.CypressCreateExecutionAction;
import uk.hmcts.zephyr.automation.cypress.actions.CypressCreateTicketAction;
import uk.hmcts.zephyr.automation.cypress.actions.CypressUpdateTicketAction;
import uk.hmcts.zephyr.automation.jira.JiraConfig;
import uk.hmcts.zephyr.automation.jira.JiraImpl;
import uk.hmcts.zephyr.automation.junit5.actions.JUnit5CreateExecutionAction;
import uk.hmcts.zephyr.automation.junit5.actions.Junit5CreateTicketAction;
import uk.hmcts.zephyr.automation.junit5.actions.Junit5UpdateTicketAction;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;
import uk.hmcts.zephyr.automation.zephyr.ZephyrImpl;

import java.time.Duration;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class Config {
    public static final String NEW_LINE_CHARACTER = "\r\n";
    public static final long DEFAULT_WAIT_TIME = Duration.ofMillis(4000).toMillis();
    public static final long DEFAULT_TIMEOUT = Duration.ofMinutes(30).toMillis();
    private static Config INSTANCE;

    private final Map<Argument, String> arguments;
    private final JiraImpl jira;
    private final ZephyrImpl zephyr;
    private final ObjectMapper objectMapper;

    public static void instantiate(String[] args) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Config has already been instantiated");
        }
        INSTANCE = new Config(args);
    }

    @Getter
    public enum Argument {
        ACTION_TYPE("action-type="),
        PROCESS_TYPE("process-type="),
        BASE_PATH("base-path="),
        REPORT_PATH("report-path="),
        GITHUB_REPO_BASE_SRC_DIR("github-repo-base-src-dir="),
        EXECUTION_ENVIRONMENT("execution-environment="),
        EXECUTION_BUILD("execution-build="),
        EXECUTION_TEST_CYCLE_NAME("execution-test-cycle-name="),
        EXECUTION_TEST_CYCLE_DESCRIPTION("execution-test-cycle-description="),
        EXECUTION_TEST_CYCLE_VERSION("execution-test-cycle-version="),
        EXECUTION_ATTACH_EVIDENCE("execution-attach-evidence="),
        ZEPHYR_ACCESS_KEY("zephyr-access-key="),
        ZEPHYR_SECRET_KEY("zephyr-secret-key="),
        ZEPHYR_ACCOUNT_ID("zephyr-account-id="),
        SUCCESS_STATUS_ID("success-status-id="),
        FAILED_STATUS_ID("failed-status-id=");

        private final String prefix;

        Argument(String prefix) {
            this.prefix = prefix;
        }
    }


    private Config(String[] args) {
        Map<Argument, String> argumentMap = new EnumMap<>(Argument.class);
        for (String arg : args) {
            for (Argument argument : Argument.values()) {
                if (arg.startsWith(argument.getPrefix())) {
                    String value = arg.substring(argument.getPrefix().length()).trim();
                    if (!value.isEmpty()) {
                        argumentMap.put(argument, value);
                    }
                }
            }
        }
        this.arguments = Collections.unmodifiableMap(argumentMap);
        if (!argumentMap.containsKey(Argument.ACTION_TYPE) || !argumentMap.containsKey(Argument.PROCESS_TYPE)) {
            throw new IllegalArgumentException(
                "Both action-type and process-type must be specified as command line arguments");
        }

        boolean hasFailedStatusId = argumentMap.containsKey(Argument.FAILED_STATUS_ID);
        boolean hasSuccessStatusId = argumentMap.containsKey(Argument.SUCCESS_STATUS_ID);
        if (hasFailedStatusId != hasSuccessStatusId) {
            throw new IllegalArgumentException(
                "Failed status id and success status id must be specified together as command line arguments");
        }

        JiraConfig.instantiate(args);

        boolean hasZephyrAccessKey = argumentMap.containsKey(Argument.ZEPHYR_ACCESS_KEY);
        boolean hasZephyrSecretKey = argumentMap.containsKey(Argument.ZEPHYR_SECRET_KEY);
        boolean hasZephyrAccountId = argumentMap.containsKey(Argument.ZEPHYR_ACCOUNT_ID);
        if (!hasZephyrAccessKey || !hasZephyrSecretKey || !hasZephyrAccountId) {
            throw new IllegalArgumentException(
                "Zephyr configuration requires zephyr-access-key, zephyr-secret-key, zephyr-account-id"
                    + " to be specified as command line arguments");
        }

        this.objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.jira = new JiraImpl(objectMapper,
            JiraConfig.getBaseUrl(),
            JiraConfig.getAuthUsername(),
            JiraConfig.getAuthToken());
        this.zephyr = new ZephyrImpl(
            objectMapper,
            ZephyrConstants.BASE_URL,
            argumentMap.get(Argument.ZEPHYR_ACCESS_KEY),
            argumentMap.get(Argument.ZEPHYR_SECRET_KEY),
            argumentMap.get(Argument.ZEPHYR_ACCOUNT_ID)
        );
    }

    public static ProcessType getProcessType() {
        return ProcessType.valueOf(INSTANCE.arguments.getOrDefault(Argument.PROCESS_TYPE, null));
    }

    public static String getSuccessStatusId() {
        return INSTANCE.arguments.getOrDefault(Argument.SUCCESS_STATUS_ID, null);
    }

    public static String getFailedStatusId() {
        return INSTANCE.arguments.getOrDefault(Argument.FAILED_STATUS_ID, null);
    }

    public static String getZephyrAccessKey() {
        return INSTANCE.arguments.getOrDefault(Argument.ZEPHYR_ACCESS_KEY, null);
    }

    public static String getZephyrSecretKey() {
        return INSTANCE.arguments.getOrDefault(Argument.ZEPHYR_SECRET_KEY, null);
    }

    public static String getZephyrAccountId() {
        return INSTANCE.arguments.getOrDefault(Argument.ZEPHYR_ACCOUNT_ID, null);
    }

    public static ActionType getActionType() {
        return ActionType.valueOf(INSTANCE.arguments.getOrDefault(Argument.ACTION_TYPE, null));
    }

    public static String getBasePath() {
        return INSTANCE.arguments.getOrDefault(Argument.BASE_PATH, null);
    }

    public static String getReportPath() {
        return INSTANCE.arguments.getOrDefault(Argument.REPORT_PATH, null);
    }

    public static String getGithubRepoBaseSrcDir() {
        return INSTANCE.arguments.getOrDefault(Argument.GITHUB_REPO_BASE_SRC_DIR, null);
    }

    public static String getExecutionEnvironment() {
        return INSTANCE.arguments.getOrDefault(Argument.EXECUTION_ENVIRONMENT, null);
    }

    public static String getExecutionBuild() {
        return INSTANCE.arguments.getOrDefault(Argument.EXECUTION_BUILD, null);
    }

    public static String getTestCycleName() {
        return INSTANCE.arguments.getOrDefault(Argument.EXECUTION_TEST_CYCLE_NAME, null);
    }

    public static boolean shouldAttachEvidence() {
        return Boolean.parseBoolean(INSTANCE.arguments.getOrDefault(Argument.EXECUTION_ATTACH_EVIDENCE, "false"));
    }

    public static String getTestCycleVersion() {
        return INSTANCE.arguments.getOrDefault(Argument.EXECUTION_TEST_CYCLE_VERSION, null);
    }

    public static String getTestCycleDescription() {
        return INSTANCE.arguments.getOrDefault(Argument.EXECUTION_TEST_CYCLE_DESCRIPTION, null);
    }

    public static JiraImpl getJira() {
        return INSTANCE.jira;
    }

    public static ZephyrImpl getZephyr() {
        return INSTANCE.zephyr;
    }

    public static ObjectMapper getObjectMapper() {
        return INSTANCE.objectMapper;
    }


    public enum ActionType {
        CREATE_TICKETS,
        UPDATE_TICKETS,
        CREATE_EXECUTION
    }

    @RequiredArgsConstructor
    public enum ProcessType {
        CUCUMBER_JSON_REPORT(Map.of(
            ActionType.CREATE_TICKETS, CucumberCreateTicketAction::new,
            ActionType.UPDATE_TICKETS, CucumberUpdateTicketAction::new,
            ActionType.CREATE_EXECUTION, CucumberCreateExecutionAction::new
        )),
        CYPRESS_JSON_REPORT(Map.of(
            ActionType.CREATE_TICKETS, CypressCreateTicketAction::new,
            ActionType.UPDATE_TICKETS, CypressUpdateTicketAction::new,
            ActionType.CREATE_EXECUTION, CypressCreateExecutionAction::new
        )),
        JUNIT5_JSON_REPORT(Map.of(
            ActionType.CREATE_TICKETS, Junit5CreateTicketAction::new,
            ActionType.UPDATE_TICKETS, Junit5UpdateTicketAction::new,
            ActionType.CREATE_EXECUTION, JUnit5CreateExecutionAction::new
        ));


        private final Map<ActionType, Supplier<Action>> actionTypeSupplierMap;

        public void processAction(ActionType actionType) {
            Supplier<Action> actionSupplier = actionTypeSupplierMap.get(actionType);
            if (actionSupplier == null) {
                throw new UnsupportedOperationException(
                    "Unsupported action type: " + actionType + " for process type: " + this);
            }
            log.info("Processing action type: {} for process type: {}", actionType, this);
            actionSupplier.get().process();
        }
    }
}

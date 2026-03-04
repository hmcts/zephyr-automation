package uk.hmcts.zephyr.automation;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.hmcts.zephyr.automation.jira.client.Jira;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;
import uk.hmcts.zephyr.automation.zephyr.ZephyrImpl;
import uk.hmcts.zephyr.automation.zephyr.client.Zephyr;

import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class Config {
    public static final String NEW_LINE_CHARACTER = "\r\n";
    public static final long DEFAULT_WAIT_TIME = Duration.ofMillis(500).toMillis();
    public static final long DEFAULT_TIMEOUT = Duration.ofMinutes(2).toMillis();
    private static Config INSTANCE;

    private final ProcessType processType;
    private final ActionType actionType;
    private final String basePath;
    private final String reportPath;
    private final String githubRepoBaseSrcDir;
    private final Jira jira;
    private final Zephyr zephyr;
    private final ObjectMapper objectMapper;
    private final String executionEnvironment;
    private final String executionBuild;
    private final String testCycleName;
    private final boolean attachEvidence;

    public static void instantiate(String[] args) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Config has already been instantiated");
        }
        INSTANCE = new Config(args);
    }

    private Config(String[] args) {
        Config.ActionType actionType = null;
        Config.ProcessType processType = null;
        String basePath = null;
        String reportPath = null;
        String githubRepoBaseSrcDir = null;
        String executionEnvironment = null;
        String executionBuild = null;
        String testCycleName = null;
        boolean attachEvidence = false;

        for (String arg : args) {
            if (arg.startsWith("action-type=")) {
                actionType = Config.ActionType.valueOf(arg.substring("action-type=".length()));
            } else if (arg.startsWith("process-type=")) {
                processType = Config.ProcessType.valueOf(arg.substring("process-type:".length()));
            } else if (arg.startsWith("base-path=")) {
                basePath = arg.substring("base-path=".length());
            } else if (arg.startsWith("github-repo-base-src-dir=")) {
                githubRepoBaseSrcDir = arg.substring("github-repo-base-src-dir=".length());
            } else if (arg.startsWith("report-path=")) {
                reportPath = arg.substring("report-path=".length());
            } else if (arg.startsWith("execution-environment=")) {
                executionEnvironment = arg.substring("execution-environment=".length());
            } else if (arg.startsWith("execution-build=")) {
                executionBuild = arg.substring("execution-build=".length());
            } else if (arg.startsWith("execution-test-cycle-name=")) {
                testCycleName = arg.substring("execution-test-cycle-name=".length());
            } else if (arg.startsWith("execution-attach-evidence=")) {
                attachEvidence = Boolean.parseBoolean(arg.substring("execution-attach-evidence=".length()));
            }
        }

        if (actionType == null || processType == null) {
            throw new IllegalArgumentException(
                "Both action-type and process-type must be specified as command line arguments");
        }
        this.actionType = actionType;
        this.processType = processType;
        this.basePath = basePath;
        this.reportPath = reportPath;
        this.githubRepoBaseSrcDir = githubRepoBaseSrcDir;
        this.executionEnvironment = executionEnvironment;
        this.executionBuild = executionBuild;
        this.testCycleName = testCycleName;
        this.attachEvidence = attachEvidence;
        JiraConfig.instantiate(args);

        this.objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.jira = new JiraImpl(objectMapper, JiraConfig.getBaseUrl(), JiraConfig.getAuthToken());
        this.zephyr = new ZephyrImpl(objectMapper, ZephyrConstants.BASE_URL, JiraConfig.getAuthToken());
    }

    public static ProcessType getProcessType() {
        return INSTANCE.processType;
    }

    public static ActionType getActionType() {
        return INSTANCE.actionType;
    }

    public static String getBasePath() {
        return INSTANCE.basePath;
    }

    public static String getReportPath() {
        return INSTANCE.reportPath;
    }

    public static String getGithubRepoBaseSrcDir() {
        return INSTANCE.githubRepoBaseSrcDir;
    }

    public static Jira getJira() {
        return INSTANCE.jira;
    }

    public static Zephyr getZephyr() {
        return INSTANCE.zephyr;
    }

    public static ObjectMapper getObjectMapper() {
        return INSTANCE.objectMapper;
    }

    public static String getExecutionEnvironment() {
        return INSTANCE.executionEnvironment;
    }

    public static String getExecutionBuild() {
        return INSTANCE.executionBuild;
    }

    public static String getTestCycleName() {
        return INSTANCE.testCycleName;
    }

    public static boolean shouldAttachEvidence() {
        return INSTANCE.attachEvidence;
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

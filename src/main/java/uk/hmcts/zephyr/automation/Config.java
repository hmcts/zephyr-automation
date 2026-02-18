package uk.hmcts.zephyr.automation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;
import uk.hmcts.zephyr.automation.actions.Action;
import uk.hmcts.zephyr.automation.cucumber.actions.CucumberCreateExecutionAction;
import uk.hmcts.zephyr.automation.cucumber.actions.CucumberCreateTicketAction;
import uk.hmcts.zephyr.automation.cypress.actions.CypressCreateExecutionAction;
import uk.hmcts.zephyr.automation.cypress.actions.CypressCreateTicketAction;
import uk.hmcts.zephyr.automation.jira.JiraImpl;
import uk.hmcts.zephyr.automation.jira.client.Jira;
import uk.hmcts.zephyr.automation.zephyr.ZephyrImpl;
import uk.hmcts.zephyr.automation.zephyr.client.Zephyr;

import java.util.Map;
import java.util.function.Function;

@Slf4j
public class Config {
    public static ProcessType processType = null;
    public static ActionType actionType = null;
    public static String basePath = null;
    public static String reportPath = null;
    public static String githubRepoBaseSrcDir = null;

    public static final Jira JIRA = new JiraImpl();
    public static final Zephyr ZEPHYR = new ZephyrImpl();
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public enum ActionType {
        CREATE_TICKETS,
        CREATE_EXECUTION
    }

    @RequiredArgsConstructor
    public enum ProcessType {
        CUCUMBER_JSON_REPORT(Map.of(
            ActionType.CREATE_TICKETS, CucumberCreateTicketAction::new,
            ActionType.CREATE_EXECUTION, CucumberCreateExecutionAction::new
        )),
        CYPRESS_JSON_REPORT(Map.of(
            ActionType.CREATE_TICKETS, CypressCreateTicketAction::new,
            ActionType.CREATE_EXECUTION, CypressCreateExecutionAction::new
        ));


        private final Map<ActionType, Function<String[], Action>> createTicketAction;

        public void processAction(ActionType actionType, String[] args) {
            Function<String[], Action> actionFunction = createTicketAction.get(actionType);
            if (actionFunction == null) {
                throw new UnsupportedOperationException(
                    "Unsupported action type: " + actionType + " for process type: " + this);
            }
            log.info("Processing action type: {} for process type: {}", actionType, this);
            actionFunction.apply(args).process();
        }
    }
}

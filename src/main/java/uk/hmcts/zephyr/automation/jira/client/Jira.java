package uk.hmcts.zephyr.automation.jira.client;

import uk.hmcts.zephyr.automation.jira.models.JiraComponent;

public interface Jira extends JiraClient {
    JiraComponent getComponentByName(String projectId, String componentName);
}

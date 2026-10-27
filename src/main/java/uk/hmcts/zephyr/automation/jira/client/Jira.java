package uk.hmcts.zephyr.automation.jira.client;

import uk.hmcts.zephyr.automation.jira.models.JiraComponent;
import uk.hmcts.zephyr.automation.jira.models.JiraIssue;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueFieldsWrapper;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueLink;
import uk.hmcts.zephyr.automation.jira.models.JiraSearchRequest;
import uk.hmcts.zephyr.automation.jira.models.JiraSearchResponse;
import uk.hmcts.zephyr.automation.jira.models.JiraTransitionRequest;

import java.util.List;

public interface Jira {

    JiraIssue createIssue(JiraIssueFieldsWrapper issue);

    JiraComponent getComponentByName(String projectId, String componentName);

    List<JiraComponent> getProjectComponents(String projectId);

    void linkIssue(JiraIssueLink jiraIssueLink);

    JiraSearchResponse searchIssues(JiraSearchRequest searchRequest);

    JiraIssue updateIssue(JiraIssueFieldsWrapper body, String issueId);

    void transitionIssue(String issueId, JiraTransitionRequest transitionRequest);
}

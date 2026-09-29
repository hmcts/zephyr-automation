package uk.hmcts.zephyr.automation.jira.client;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import uk.hmcts.zephyr.automation.jira.models.JiraComponent;
import uk.hmcts.zephyr.automation.jira.models.JiraIssue;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueFieldsWrapper;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueLink;
import uk.hmcts.zephyr.automation.jira.models.JiraSearchRequest;
import uk.hmcts.zephyr.automation.jira.models.JiraSearchResponse;
import uk.hmcts.zephyr.automation.jira.models.JiraTransitionRequest;

import java.util.List;
import java.util.Map;

public interface JiraClient {

    @RequestLine("POST /issue")
    JiraIssue createIssue(JiraIssueFieldsWrapper issue);

    @RequestLine("GET /project/{projectId}/components")
    List<JiraComponent> getProjectComponents(@Param("projectId") String projectId);

    @RequestLine("POST /issueLink")
    void linkIssue(JiraIssueLink jiraIssueLink);

    @RequestLine("GET /search/jql")
    @Headers("Accept: application/json")
    JiraSearchResponse searchIssues(@QueryMap Map<String, Object> queryParams);

    @RequestLine("PUT /issue/{issueId}")
    JiraIssue updateIssue(JiraIssueFieldsWrapper body, @Param("issueId") String issueId);

    @RequestLine("POST /issue/{issueId}/transitions")
    void transitionIssue(@Param("issueId") String issueId, JiraTransitionRequest transitionRequest);
}

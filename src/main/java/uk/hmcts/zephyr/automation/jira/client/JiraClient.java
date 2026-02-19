package uk.hmcts.zephyr.automation.jira.client;

import feign.Param;
import feign.RequestLine;
import uk.hmcts.zephyr.automation.jira.models.JiraComponent;
import uk.hmcts.zephyr.automation.jira.models.JiraIssue;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueFieldsWrapper;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueLink;
import uk.hmcts.zephyr.automation.jira.models.JiraSearchRequest;
import uk.hmcts.zephyr.automation.jira.models.JiraSearchResponse;

import java.util.List;

public interface JiraClient {

    @RequestLine("POST /issue")
    JiraIssue createIssue(JiraIssueFieldsWrapper issue);

    @RequestLine("GET /project/{projectId}/components")
    List<JiraComponent> getProjectComponents(@Param("projectId") String projectId);

    @RequestLine("POST /issueLink")
    void linkIssue(JiraIssueLink jiraIssueLink);

    @RequestLine("POST /search")
    JiraSearchResponse searchIssues(JiraSearchRequest searchRequest);

    @RequestLine("PUT /issue/{issueId}")
    JiraIssue updateIssue(JiraIssueFieldsWrapper body, @Param("issueId") String issueId);
}

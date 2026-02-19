package uk.hmcts.zephyr.automation.jira;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.jira.client.Jira;
import uk.hmcts.zephyr.automation.jira.client.JiraClient;
import uk.hmcts.zephyr.automation.jira.models.JiraComponent;
import uk.hmcts.zephyr.automation.jira.models.JiraIssue;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueFieldsWrapper;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueLink;
import uk.hmcts.zephyr.automation.jira.models.JiraSearchRequest;
import uk.hmcts.zephyr.automation.jira.models.JiraSearchResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class JiraImpl implements Jira {
    private final JiraClient jiraClient;


    private final Map<String, List<JiraComponent>> componentsCacheMap;

    public JiraImpl(ObjectMapper objectMapper, String baseUrl, String authToken) {
        jiraClient = Feign.builder()
            .requestInterceptor(template -> {
                template.header("Authorization", authToken);
                template.header("Content-Type", "application/json");
            })
            .encoder(new JacksonEncoder(objectMapper))
            .decoder(new JacksonDecoder(objectMapper))
            .logLevel(Logger.Level.FULL)
            .logger(new Slf4jLogger())
            .target(JiraClient.class, baseUrl);

        componentsCacheMap = new HashMap<>();
    }


    @Override
    public List<JiraComponent> getProjectComponents(String projectId) {
        return componentsCacheMap.computeIfAbsent(projectId, jiraClient::getProjectComponents);
    }

    @Override
    public JiraComponent getComponentByName(String projectId, String componentName) {
        return getProjectComponents(projectId).stream()
            .filter(component -> component.getName().equalsIgnoreCase(componentName))
            .findFirst()
            .orElse(null);
    }

    //Passthrough methods
    @Override
    public JiraIssue createIssue(JiraIssueFieldsWrapper issue) {
        return jiraClient.createIssue(issue);
    }

    @Override
    public void linkIssue(JiraIssueLink jiraIssueLink) {
        jiraClient.linkIssue(jiraIssueLink);
    }

    @Override
    @SneakyThrows
    public JiraSearchResponse searchIssues(JiraSearchRequest searchRequest) {
        return jiraClient.searchIssues(searchRequest);
    }

    @Override
    @SneakyThrows
    public JiraIssue updateIssue(JiraIssueFieldsWrapper body, String issueId) {
        return jiraClient.updateIssue(body, issueId);
    }
}

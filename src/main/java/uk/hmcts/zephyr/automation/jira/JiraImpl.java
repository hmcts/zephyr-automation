package uk.hmcts.zephyr.automation.jira;

import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.jira.client.Jira;
import uk.hmcts.zephyr.automation.jira.client.JiraClient;
import uk.hmcts.zephyr.automation.jira.models.JiraComponent;
import uk.hmcts.zephyr.automation.jira.models.JiraIssue;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueFieldsWrapper;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueLink;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class JiraImpl implements Jira {
    private final JiraClient jiraClient;


    private final Map<String, List<JiraComponent>> componentsCacheMap;

    public JiraImpl() {
        jiraClient = Feign.builder()
            .requestInterceptor(template -> {
                template.header("Authorization", "Bearer " + JiraConstants.AUTH_TOKEN);
                template.header("Content-Type", "application/json");
            })
            .encoder(new JacksonEncoder())
            .decoder(new JacksonDecoder())
            .logLevel(Logger.Level.FULL)
            .logger(new Slf4jLogger())
            .target(JiraClient.class, JiraConstants.BASE_URL);

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
}

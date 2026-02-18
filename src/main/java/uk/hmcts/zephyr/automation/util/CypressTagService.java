package uk.hmcts.zephyr.automation.util;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.cypress.models.CypressReport;

import java.util.List;
import java.util.Optional;

import static uk.hmcts.zephyr.automation.jira.JiraConfig.JIRA_KEY_TAG_PREFIX;

@Slf4j
public class CypressTagService {

    public static Optional<String> extractJiraKeyFromTag(CypressReport.CypressTest test) {
        return extractTagWithPrefix(test, JIRA_KEY_TAG_PREFIX)
            .map(key -> key.replace(JIRA_KEY_TAG_PREFIX, ""));
    }

    public static Optional<String> extractTagWithPrefix(CypressReport.CypressTest test, String prefix) {
        return extractTagListWithPrefix(test, prefix).stream().findFirst();
    }

    public static List<String> extractTagListWithPrefix(CypressReport.CypressTest test, String prefix) {
        return test.getTags().stream()
            .filter(tag -> tag.startsWith(prefix))
            .map(tag -> tag.substring(prefix.length()))
            .toList();
    }


}

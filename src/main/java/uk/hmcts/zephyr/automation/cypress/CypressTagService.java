package uk.hmcts.zephyr.automation.cypress;

import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.cypress.models.CypressReport;

import java.util.List;
import java.util.Optional;

import static uk.hmcts.zephyr.automation.jira.JiraConstants.JIRA_KEY_TAG_PREFIX;

public class CypressTagService implements TagService<CypressReport.CypressTest> {
    @Override
    public Optional<String> extractJiraKeyFromTag(CypressReport.CypressTest test) {
        return extractTagWithPrefix(test, JIRA_KEY_TAG_PREFIX)
            .map(key -> key.replace(JIRA_KEY_TAG_PREFIX, ""));
    }

    @Override
    public Optional<String> extractTagWithPrefix(CypressReport.CypressTest test, String prefix) {
        return extractTagListWithPrefix(test, prefix).stream().findFirst();
    }

    @Override
    public List<String> extractTagListWithPrefix(CypressReport.CypressTest test, String prefix) {
        return test.getTags().stream()
            .filter(tag -> tag.startsWith(prefix))
            .map(tag -> tag.substring(prefix.length()))
            .toList();
    }

    @Override
    public void addTag(CypressReport.CypressTest test, String tagName) {
        //TODO
    }
}

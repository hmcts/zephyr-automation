package uk.hmcts.zephyr.automation.cypress;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.cypress.models.CypressReport;

import java.util.List;
import java.util.Optional;

import static uk.hmcts.zephyr.automation.jira.JiraConfig.JIRA_KEY_TAG_PREFIX;

@Slf4j
public class CypressTagService implements TagService<CypressReport.CypressTest> {

    private String addTagPrefix(String tagName) {
        return "@" + tagName;
    }

    @Override
    public Optional<String> extractJiraKeyFromTag(CypressReport.CypressTest test) {
        return extractTagWithPrefix(test, JIRA_KEY_TAG_PREFIX)
            .map(key -> key.replace(addTagPrefix(JIRA_KEY_TAG_PREFIX), ""));
    }

    @Override
    public Optional<String> extractTagWithPrefix(CypressReport.CypressTest test, String prefix) {
        return extractTagListWithPrefix(test, prefix).stream().findFirst();
    }

    @Override
    public List<String> extractTagListWithPrefix(CypressReport.CypressTest test, String prefix) {
        return test.getTags().stream()
            .filter(tag -> tag.startsWith(addTagPrefix(prefix)))
            .map(tag -> tag.substring(addTagPrefix(prefix).length()))
            .toList();
    }


    @Override
    public void addTag(CypressReport.CypressTest test, String tagNameBase) {
        String tagName = addTagPrefix(tagNameBase);
        if (test.hasTag(tagName)) {
            log.info("Test '{}' already has tag '{}', skipping addition.", test.getLocationDisplayName(), tagName);
            return;
        }
        String fileLocation = Config.getBasePath() + "/" + test.getFile();
        // Use hardcoded file path for now (update as needed)
        log.info("Added tag '{}' to test '{}' in file '{}'", tagName, test.getLocationDisplayName(), fileLocation);

        CypressTagger.addTagToCypressTest(
            fileLocation,
            test.getTitle(),
            tagName
        );
        test.addTag(tagName);

    }
}

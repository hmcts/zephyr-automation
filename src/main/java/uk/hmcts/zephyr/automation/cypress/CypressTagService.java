package uk.hmcts.zephyr.automation.cypress;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.cypress.models.CypressReport;
import uk.hmcts.zephyr.util.FileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.hmcts.zephyr.automation.jira.JiraConstants.JIRA_KEY_TAG_PREFIX;

@Slf4j
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
        if (test.hasTag(tagName)) {
            log.info("Test '{}' already has tag '{}', skipping addition.", test.getLocationDisplayName(), tagName);
            return;
        }
        String fileLocation = Config.basePath + "/" + test.getFile();
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

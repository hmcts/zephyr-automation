package uk.hmcts.zephyr.automation.cypress;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.TestTag;
import uk.hmcts.zephyr.automation.cypress.models.CypressReport;

import java.util.List;

@Slf4j
public class CypressTagService implements TagService<CypressReport.CypressTest> {

    public CypressTagService() {
    }

    @Override
    public List<TestTag> extractTagListFromType(CypressReport.CypressTest test, TestTag.Type tagType) {
        String prefix = getTagPrefix(tagType);
        return test.getTags().stream()
            .filter(tag -> tag.startsWith(prefix))
            .map(tag -> tag.substring(prefix.length()))
            .map(value -> new TestTag(tagType, value))
            .toList();
    }


    @Override
    public void addTag(CypressReport.CypressTest test, TestTag testTag) {
        String tagName = getTagPrefix(testTag.type());
        if (testTag.value() != null) {
            tagName = tagName + testTag.value();
        }
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

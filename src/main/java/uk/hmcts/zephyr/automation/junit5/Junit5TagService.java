package uk.hmcts.zephyr.automation.junit5;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.TestTag;
import uk.hmcts.zephyr.automation.junit5.model.Junit5ZephyrReport;

import java.util.List;

@Slf4j
public class Junit5TagService implements TagService<Junit5ZephyrReport.Test> {

    @Override
    public List<TestTag> extractTagListFromType(Junit5ZephyrReport.Test test, TestTag.Type tagType) {
        JiraAnnotationMetadata metadata = test.getMetadata();
        if (TestTag.Type.JIRA_IGNORE.equals(tagType)) {
            return List.of(new TestTag(TestTag.Type.JIRA_IGNORE, String.valueOf(metadata.jiraIgnore())));
        }

        List<String> values = switch (tagType) {
            case JIRA_KEY -> metadata.jiraKey();
            case JIRA_COMPONENT -> metadata.jiraComponents();
            case JIRA_LABEL -> metadata.jiraLabels();
            case JIRA_EPIC -> metadata.jiraEpics();
            case JIRA_NFR -> metadata.jiraNfrs();
            case JIRA_LINK -> metadata.jiraLinks();
            case JIRA_STORY -> metadata.jiraStories();
            case JIRA_DEFECT -> metadata.jiraDefects();
            default -> throw new UnsupportedOperationException("Unknown tag type: " + tagType);
        };
        return values.stream()
            .map(value -> new TestTag(tagType, value))
            .toList();
    }

    @Override
    public void addTag(Junit5ZephyrReport.Test test, TestTag testTag) {

    }
}

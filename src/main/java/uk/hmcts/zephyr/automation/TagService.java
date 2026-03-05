package uk.hmcts.zephyr.automation;

import java.util.List;
import java.util.Optional;

public interface TagService<T> {
    default Optional<String> extractJiraKeyFromTag(T test) {
        return extractTagFromTagType(test, TestTag.Type.JIRA_KEY)
            .map(TestTag::value);
    }

    default Optional<TestTag> extractTagFromTagType(T test, TestTag.Type tagType) {
        return extractTagListFromType(test, tagType).stream().findFirst();
    }

    List<TestTag> extractTagListFromType(T test, TestTag.Type tagType);

    void addTag(T test, TestTag testTag);

    default boolean hasTag(T test, TestTag.Type tagType) {
        return extractTagFromTagType(test, tagType).isPresent();
    }

    default String getTagPrefix(TestTag.Type tagType) {
        return switch (tagType) {
            case JIRA_KEY -> "@JIRA-KEY:";
            case JIRA_COMPONENT -> "@JIRA-COMPONENT:";
            case JIRA_LABEL -> "@JIRA-LABEL:";
            case JIRA_EPIC -> "@JIRA-EPIC:";
            case JIRA_NFR -> "@JIRA-NFR:";
            case JIRA_LINK -> "@JIRA-LINK:";
            case JIRA_STORY -> "@JIRA-STORY:";
            case JIRA_DEFECT -> "@JIRA-DEFECT:";
            case JIRA_IGNORE -> "@JIRA-IGNORE";
        };
    }
}

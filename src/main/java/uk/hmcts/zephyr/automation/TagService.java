package uk.hmcts.zephyr.automation;

import java.util.List;
import java.util.Optional;

public interface TagService<T> {
    default Optional<String> extractJiraKeyFromTag(T test) {
        return extractTagFromTagType(test, TestTag.Type.JIRA_KEY)
            .map(TestTag::value);
    }

    Optional<TestTag> extractTagFromTagType(T test, TestTag.Type tagType);

    List<TestTag> extractTagListFromType(T test, TestTag.Type tagType);

    void addTag(T test, TestTag testTag);

    default boolean hasTag(T test, TestTag.Type tagType) {
        return extractTagFromTagType(test, tagType).isPresent();
    }
}

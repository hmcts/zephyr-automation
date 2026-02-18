package uk.hmcts.zephyr.automation;

import java.util.List;
import java.util.Optional;

public interface TagService<T> {
    Optional<String> extractJiraKeyFromTag(T test);

    Optional<String> extractTagWithPrefix(T test, String prefix);

    List<String> extractTagListWithPrefix(T test, String prefix);

    void addTag(T test, String tagName);
}

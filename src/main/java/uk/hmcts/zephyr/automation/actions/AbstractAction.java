package uk.hmcts.zephyr.automation.actions;

import lombok.Getter;
import uk.hmcts.zephyr.automation.TagService;

@Getter
public abstract class AbstractAction<T extends ZephyrTest> implements Action {
    private final TagService<T> tagService;

    protected AbstractAction(TagService<T> tagService) {
        this.tagService = tagService;
    }
}

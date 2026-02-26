package uk.hmcts.zephyr.automation.actions;

import org.junit.jupiter.api.Test;
import uk.hmcts.zephyr.automation.TagService;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class AbstractActionTest {

    @SuppressWarnings("unchecked")
    @Test
    void given_tagService_when_constructed_then_exposesTagService() {
        TagService<ZephyrTest> tagService = mock(TagService.class);

        TestAction action = new TestAction(tagService);

        assertSame(tagService, action.getTagService());
    }

    private static class TestAction extends AbstractAction<ZephyrTest> {

        TestAction(TagService<ZephyrTest> tagService) {
            super(tagService);
        }

        @Override
        public void process() {
            // no-op: concrete action under test
        }
    }
}


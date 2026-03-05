package uk.hmcts.zephyr.automation.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraKey;

class SampleNestedTest {

    @Nested
    class Nested1 {

        @Nested
        class Nested2 {

            @Test
            @JiraKey("ABC-123")
            void targetMethod() {
            }
        }
    }
}


package uk.hmcts.zephyr.automation.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

class SampleNestedTest {

    @Nested
    class Nested1 {

        @Nested
        class Nested2 {

            @Test
            @JiraTestKey("ABC-123")
            void targetMethod() {
            }
        }
    }
}


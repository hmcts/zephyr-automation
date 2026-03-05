package uk.hmcts.zephyr.automation.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SampleNestedTest {

    @Nested
    class Nested1 {

        @Nested
        class Nested2 {

            @Test
            void targetMethod() {
            }
        }
    }
}


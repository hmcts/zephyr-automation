package uk.hmcts.zephyr.automation.junit5.extension;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZephyrAutomationExtensionAggregatorTest {

    @Nested
    class ToDeterministicArgumentStringTest {

        @Test
        void givenNullValue_whenFormatting_thenReturnsLiteralNull() {
            String result = ZephyrAutomationExtension.toDeterministicArgumentString(null);

            assertEquals("null", result);
        }

        @Test
        void givenValueWithDefaultToString_whenFormatting_thenUsesDeterministicJson() {
            DefaultToStringArgument value = new DefaultToStringArgument("alpha", 42);

            String result = ZephyrAutomationExtension.toDeterministicArgumentString(value);

            assertEquals("{\"count\":42,\"name\":\"alpha\"}", result);
        }

        @Test
        void givenValueWithCustomToString_whenFormatting_thenUsesCustomString() {
            CustomToStringArgument value = new CustomToStringArgument("fixed-value");

            String result = ZephyrAutomationExtension.toDeterministicArgumentString(value);

            assertEquals("custom:fixed-value", result);
        }
    }

    static final class DefaultToStringArgument {
        private final String name;
        private final int count;

        DefaultToStringArgument(String name, int count) {
            this.name = name;
            this.count = count;
        }

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }
    }

    static final class CustomToStringArgument {
        private final String value;

        CustomToStringArgument(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "custom:" + value;
        }
    }
}




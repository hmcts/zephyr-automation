package uk.hmcts.zephyr.automation.zephyr.models;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZephyrBulkExecutionResponseTest {

    @Nested
    class IsCompletedTest {

        @Test
        void given_progressIsNull_when_isCompleted_then_returnsFalse() {
            ZephyrBulkExecutionResponse response = ZephyrBulkExecutionResponse.builder().build();

            assertFalse(response.isCompleted());
        }

        @Test
        void given_progressIsLessThanOne_when_isCompleted_then_returnsFalse() {
            ZephyrBulkExecutionResponse response = ZephyrBulkExecutionResponse.builder()
                .progress(0.5)
                .build();

            assertFalse(response.isCompleted());
        }

        @Test
        void given_progressIsExactlyOne_when_isCompleted_then_returnsTrue() {
            ZephyrBulkExecutionResponse response = ZephyrBulkExecutionResponse.builder()
                .progress(1.0)
                .build();

            assertTrue(response.isCompleted());
        }

        @Test
        void given_progressIsGreaterThanOne_when_isCompleted_then_returnsTrue() {
            ZephyrBulkExecutionResponse response = ZephyrBulkExecutionResponse.builder()
                .progress(1.5)
                .build();

            assertTrue(response.isCompleted());
        }

        @Test
        void given_progressIsZero_when_isCompleted_then_returnsFalse() {
            ZephyrBulkExecutionResponse response = ZephyrBulkExecutionResponse.builder()
                .progress(0.0)
                .build();

            assertFalse(response.isCompleted());
        }
    }

    @Nested
    class IsFailedTest {

        @Test
        void given_errorMessageIsNull_when_isFailed_then_returnsFalse() {
            ZephyrBulkExecutionResponse response = ZephyrBulkExecutionResponse.builder().build();

            assertFalse(response.isFailed());
        }

        @Test
        void given_errorMessageIsEmpty_when_isFailed_then_returnsFalse() {
            ZephyrBulkExecutionResponse response = ZephyrBulkExecutionResponse.builder()
                .errorMessage("")
                .build();

            assertFalse(response.isFailed());
        }

        @Test
        void given_errorMessageIsPopulated_when_isFailed_then_returnsTrue() {
            ZephyrBulkExecutionResponse response = ZephyrBulkExecutionResponse.builder()
                .errorMessage("Something went wrong")
                .build();

            assertTrue(response.isFailed());
        }
    }
}


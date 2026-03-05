package uk.hmcts.zephyr.automation.junit5.actions;

import org.junit.jupiter.api.Test;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.junit5.model.Junit5ZephyrReport;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Junit5ActionJiraDescriptionTest {

    private final Junit5ActionJiraDescription description = new Junit5ActionJiraDescription() { };

    @Test
    void jiraDescriptionPostProcess_appendsFormattedDetailsBlock() {
        Junit5ZephyrReport.Test test = new Junit5ZephyrReport.Test(
            "ZE-123",
            "Sample test",
            "uk.hmcts.zephyr.automation.Sample",
            "shouldDoSomething",
            Junit5ZephyrReport.Test.Status.PASSED,
            null,
            null,
            Set.of("smoke"),
            null
        );
        StringBuilder builder = new StringBuilder("existing-content");

        description.jiraDescriptionPostProcess(test, builder);

        String expected = "existing-content"
            + Config.NEW_LINE_CHARACTER
            + "*Details:*"
            + "- id: ZE-123"
            + Config.NEW_LINE_CHARACTER
            + "- Class: uk.hmcts.zephyr.automation.Sample"
            + Config.NEW_LINE_CHARACTER
            + "- Method: shouldDoSomething";

        assertEquals(expected, builder.toString());
    }

    @Test
    void jiraDescriptionPostProcess_preservesNestedClassNames() {
        Junit5ZephyrReport.Test test = new Junit5ZephyrReport.Test(
            "ID-456",
            "Nested test",
            "uk.hmcts.zephyr.automation.util.UtilTest$Nested1$Nested2",
            "nestedCase",
            Junit5ZephyrReport.Test.Status.FAILED,
            "IllegalStateException",
            "boom",
            Set.of(),
            null
        );
        StringBuilder builder = new StringBuilder();

        description.jiraDescriptionPostProcess(test, builder);

        String output = builder.toString();
        assertTrue(output.contains("- Class: " + test.getClassName()));
        assertTrue(output.endsWith("- Method: " + test.getMethodName()));
    }
}

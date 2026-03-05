package uk.hmcts.zephyr.automation.junit5.actions;

import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.junit5.model.Junit5ZephyrReport;

public interface Junit5ActionJiraDescription {
    default void jiraDescriptionPostProcess(Junit5ZephyrReport.Test test, StringBuilder builder) {
        builder
            .append(Config.NEW_LINE_CHARACTER)
            .append("*Details:*")
            .append("- id: ").append(test.getId())
            .append(Config.NEW_LINE_CHARACTER)
            .append("- Class: ").append(test.getClassName())
            .append(Config.NEW_LINE_CHARACTER)
            .append("- Method: ").append(test.getMethodName());

    }
}

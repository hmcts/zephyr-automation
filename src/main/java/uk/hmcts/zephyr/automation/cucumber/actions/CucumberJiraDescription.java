package uk.hmcts.zephyr.automation.cucumber.actions;

import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element;
import uk.hmcts.zephyr.automation.util.Util;

public interface CucumberJiraDescription {

    default void jiraDescriptionPostProcess(Element test, StringBuilder builder) {
        if (!Util.hasItems(test.getSteps())) {
            return;
        }
        builder
            .append(Config.NEW_LINE_CHARACTER)
            .append("*Scenario:*");

        test.getSteps()
            .forEach(step -> {
                    builder
                        .append(Config.NEW_LINE_CHARACTER)
                        .append("*")
                        .append(step.getKeyword().trim())
                        .append("*: ")
                        .append(step.getName());
                    //If the step has rows, add them to the description
                    if (Util.hasItems(step.getRows())) {
                        step.getRows().forEach(row -> {
                            builder.append(Config.NEW_LINE_CHARACTER)
                                .append("|");
                            row.getCells().forEach(cell -> builder.append(" ").append(cell).append(" |"));
                        });
                    }
                }
            );
    }
}

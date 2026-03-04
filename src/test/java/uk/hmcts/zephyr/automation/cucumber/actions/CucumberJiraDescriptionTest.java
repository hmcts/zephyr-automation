package uk.hmcts.zephyr.automation.cucumber.actions;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element.Step;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element.Step.Row;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.hmcts.zephyr.automation.Config.NEW_LINE_CHARACTER;

class CucumberJiraDescriptionTest {

    private final CucumberJiraDescription description = new CucumberJiraDescription() { };

    @Nested
    class JiraDescriptionPostProcessTest {

        @Test
        void givenElementWithoutSteps_whenPostProcess_thenLeavesBuilderUnchanged() {
            Element element = new Element();
            element.setSteps(null);
            StringBuilder builder = new StringBuilder("Base");

            description.jiraDescriptionPostProcess(element, builder);

            assertEquals("Base", builder.toString());
        }

        @Test
        void givenSteps_whenPostProcess_thenAppendsScenarioAndSteps() {
            Element element = new Element();
            element.setSteps(List.of(
                step(" Given ", "user enters credentials"),
                step("When", "user submits form")
            ));
            StringBuilder builder = new StringBuilder("Base");

            description.jiraDescriptionPostProcess(element, builder);

            String expected = "Base" + NEW_LINE_CHARACTER
                + "*Scenario:*" + NEW_LINE_CHARACTER
                + "*Given*: user enters credentials" + NEW_LINE_CHARACTER
                + "*When*: user submits form";

            assertEquals(expected, builder.toString());
        }

        @Test
        void givenStepWithRows_whenPostProcess_thenAppendsDataTable() {
            final Element element = new Element();
            Step thenStep = step("Then", "values are stored");
            Row firstRow = new Row();
            firstRow.setCells(List.of("header1", "header2"));
            Row secondRow = new Row();
            secondRow.setCells(List.of("value1", "value2"));
            thenStep.setRows(List.of(firstRow, secondRow));
            element.setSteps(List.of(thenStep));
            StringBuilder builder = new StringBuilder("Base");

            description.jiraDescriptionPostProcess(element, builder);

            String expected = "Base" + NEW_LINE_CHARACTER
                + "*Scenario:*" + NEW_LINE_CHARACTER
                + "*Then*: values are stored" + NEW_LINE_CHARACTER
                + "| header1 | header2 |" + NEW_LINE_CHARACTER
                + "| value1 | value2 |";

            assertEquals(expected, builder.toString());
        }
    }

    private static Step step(String keyword, String name) {
        Step step = new Step();
        step.setKeyword(keyword);
        step.setName(name);
        return step;
    }
}


package uk.hmcts.zephyr.automation.cucumber.actions;

import org.junit.jupiter.api.Test;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element.Step;
import uk.hmcts.zephyr.automation.jira.models.JiraDescription;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CucumberJiraDescriptionTest {

    private final CucumberJiraDescription subject = new TestCucumberJiraDescription();

    @Test
    void givenCucumberStepsWithDataTable_whenPostProcessingDescription_thenAddsReadableStepsToJiraDescription() {
        Element element = new Element();
        Step firstStep = step("Given ", "a user exists");
        Step secondStep = step("When ", "the user submits the form");
        secondStep.setRows(List.of(
            new Step.Row(List.of("field", "value")),
            new Step.Row(List.of("name", "Jane"))
        ));
        element.setSteps(List.of(firstStep, secondStep));
        JiraDescription description = new JiraDescription();
        description.setContent(new ArrayList<>());

        subject.jiraDescriptionPostProcess(element, description);

        assertEquals(4, description.getContent().size());
        JiraDescription.ParagraphNode heading = paragraphAt(description, 0);
        assertText(heading, 0, "Steps:");
        assertEquals(JiraDescription.MarkType.STRONG, textAt(heading, 0).getMarks().getFirst().getType());

        JiraDescription.ParagraphNode firstStepParagraph = paragraphAt(description, 1);
        assertText(firstStepParagraph, 0, "Given");
        assertEquals(JiraDescription.MarkType.STRONG, textAt(firstStepParagraph, 0).getMarks().getFirst().getType());
        assertText(firstStepParagraph, 1, ": a user exists");

        JiraDescription.ParagraphNode secondStepParagraph = paragraphAt(description, 2);
        assertText(secondStepParagraph, 0, "When");
        assertText(secondStepParagraph, 1, ": the user submits the form");

        JiraDescription.TableNode table = (JiraDescription.TableNode) description.getContent().get(3);
        assertEquals("field", tableCellText(table, 0, 0));
        assertEquals("value", tableCellText(table, 0, 1));
        assertEquals("name", tableCellText(table, 1, 0));
        assertEquals("Jane", tableCellText(table, 1, 1));
    }

    @Test
    void givenNoSteps_whenPostProcessingDescription_thenLeavesDescriptionUnchanged() {
        Element element = new Element();
        JiraDescription description = new JiraDescription();
        description.setContent(new ArrayList<>());

        subject.jiraDescriptionPostProcess(element, description);

        assertEquals(List.of(), description.getContent());
    }

    private JiraDescription.ParagraphNode paragraphAt(JiraDescription description, int index) {
        return (JiraDescription.ParagraphNode) description.getContent().get(index);
    }

    private void assertText(JiraDescription.ParagraphNode paragraphNode, int index, String expectedText) {
        assertEquals(expectedText, textAt(paragraphNode, index).getText());
    }

    private JiraDescription.TextNode textAt(JiraDescription.ParagraphNode paragraphNode, int index) {
        return (JiraDescription.TextNode) paragraphNode.getContent().get(index);
    }

    private String tableCellText(JiraDescription.TableNode table, int rowIndex, int cellIndex) {
        JiraDescription.TableCellNode cell =
            (JiraDescription.TableCellNode) table.getContent().get(rowIndex).getContent().get(cellIndex);
        JiraDescription.ParagraphNode paragraph = cell.getContent().getFirst();
        return textAt(paragraph, 0).getText();
    }

    private Step step(String keyword, String name) {
        Step step = new Step();
        step.setKeyword(keyword);
        step.setName(name);
        return step;
    }

    private static class TestCucumberJiraDescription implements CucumberJiraDescription {
    }
}

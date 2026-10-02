package uk.hmcts.zephyr.automation.cucumber.actions;

import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element.Step;
import uk.hmcts.zephyr.automation.jira.models.JiraDescription;
import uk.hmcts.zephyr.automation.util.Util;

import java.util.List;

public interface CucumberJiraDescription {

    default void jiraDescriptionPostProcess(Element test, JiraDescription jiraDescription) {
        if (!Util.hasItems(test.getSteps())) {
            return;
        }
        jiraDescription.getContent().add(new JiraDescription.ParagraphNode(
            new JiraDescription.TextNode("Steps:", new JiraDescription.StrongMark())
        ));

        test.getSteps().forEach(step -> {
            jiraDescription.getContent().add(stepParagraph(step));
            if (Util.hasItems(step.getRows())) {
                jiraDescription.getContent().add(stepRowsTable(step.getRows()));
            }
        });
    }

    private JiraDescription.ParagraphNode stepParagraph(Step step) {
        String keyword = step.getKeyword() == null ? "" : step.getKeyword().trim();
        String name = step.getName() == null ? "" : step.getName();
        return new JiraDescription.ParagraphNode(
            new JiraDescription.TextNode(keyword, new JiraDescription.StrongMark()),
            new JiraDescription.TextNode(": " + name)
        );
    }

    private JiraDescription.TableNode stepRowsTable(List<Step.Row> rows) {
        return new JiraDescription.TableNode(
            rows.stream()
                .map(this::stepRow)
                .toArray(JiraDescription.TableRowNode[]::new)
        );
    }

    private JiraDescription.TableRowNode stepRow(Step.Row row) {
        return new JiraDescription.TableRowNode(
            row.getCells().stream()
                .map(this::stepCell)
                .toArray(JiraDescription.TableCellNode[]::new)
        );
    }

    private JiraDescription.TableCellNode stepCell(String value) {
        return new JiraDescription.TableCellNode(
            new JiraDescription.ParagraphNode(
                new JiraDescription.TextNode(value == null ? "" : value)
            )
        );
    }
}

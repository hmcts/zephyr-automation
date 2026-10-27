package uk.hmcts.zephyr.automation.junit5.actions;

import uk.hmcts.zephyr.automation.jira.models.JiraDescription;
import uk.hmcts.zephyr.automation.junit5.model.Junit5ZephyrReport;

import java.util.List;

public interface Junit5ActionJiraDescription {
    default void jiraDescriptionPostProcess(Junit5ZephyrReport.Test test, JiraDescription jiraDescription) {

        JiraDescription.ParagraphNode paragraphNode =  new JiraDescription.ParagraphNode(
            new JiraDescription.TextNode("Details:", new JiraDescription.StrongMark()),
            new JiraDescription.HardBreakNode()
        );

        JiraDescription.TableNode tableNode = new JiraDescription.TableNode(
            new JiraDescription.TableRowNode(
                new JiraDescription.TableHeaderNode(
                    JiraDescription.TableCellAttrs.builder()
                        .colwidth(List.of(118))
                        .build(),
                    new JiraDescription.ParagraphNode(
                        new JiraDescription.TextNode("Key", new JiraDescription.StrongMark())
                    )
                ),
                new JiraDescription.TableHeaderNode(
                    new JiraDescription.ParagraphNode(
                        new JiraDescription.TextNode("Value", new JiraDescription.StrongMark())
                    )
                )
            ),
            new JiraDescription.TableRowNode(
                new JiraDescription.TableCellNode(
                    new JiraDescription.ParagraphNode(
                        new JiraDescription.TextNode("ID")
                    )
                ),
                new JiraDescription.TableCellNode(
                    new JiraDescription.ParagraphNode(
                        new JiraDescription.TextNode(test.getId())
                    )
                )
            ),
            new JiraDescription.TableRowNode(
                new JiraDescription.TableCellNode(
                    new JiraDescription.ParagraphNode(
                        new JiraDescription.TextNode("Class Name")
                    )
                ),
                new JiraDescription.TableCellNode(
                    new JiraDescription.ParagraphNode(
                        new JiraDescription.TextNode(test.getClassName())
                    )
                )
            ),
            new JiraDescription.TableRowNode(
                new JiraDescription.TableCellNode(
                    new JiraDescription.ParagraphNode(
                        new JiraDescription.TextNode("Method Name")
                    )
                ),
                new JiraDescription.TableCellNode(
                    new JiraDescription.ParagraphNode(
                        new JiraDescription.TextNode(test.getMethodName())
                    )
                )
            )
        );

        jiraDescription.getContent().add(paragraphNode);
        jiraDescription.getContent().add(tableNode);
    }
}

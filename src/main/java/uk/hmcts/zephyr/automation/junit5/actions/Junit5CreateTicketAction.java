package uk.hmcts.zephyr.automation.junit5.actions;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.actions.AbstractCreateTicketAction;
import uk.hmcts.zephyr.automation.junit5.Junit5TagService;
import uk.hmcts.zephyr.automation.junit5.model.Junit5ZephyrReport;
import uk.hmcts.zephyr.automation.util.FileUtil;

import java.util.Objects;

@Slf4j
public class Junit5CreateTicketAction
    extends AbstractCreateTicketAction<Junit5ZephyrReport.Test>
    implements Junit5Action, Junit5ActionJiraDescription {

    public Junit5CreateTicketAction() {
        super(new Junit5TagService());
    }

    @Override
    public void process() {
        log.info("Starting Junit5 Create Ticket Action");
        Junit5ZephyrReport report = getJunit5ZephyrReport();
        if (report == null) {
            log.warn("Report is null, skipping execution creation");
            return;
        }
        report.getTests().stream()
            .filter(Objects::nonNull)
            .forEach(this::createJiraIssue);
        // Write the updated features back to the file
        FileUtil.writeToFile(Config.getReportPath(), report);
    }

    @Override
    public void jiraDescriptionPostProcess(Junit5ZephyrReport.Test test, StringBuilder builder) {
        Junit5ActionJiraDescription.super.jiraDescriptionPostProcess(test, builder);
    }
}

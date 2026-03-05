package uk.hmcts.zephyr.automation.junit5.actions;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.actions.AbstractUpdateTicketAction;
import uk.hmcts.zephyr.automation.junit5.Junit5TagService;
import uk.hmcts.zephyr.automation.junit5.model.Junit5ZephyrReport;

import java.util.Objects;

@Slf4j
public class Junit5UpdateTicketAction
    extends AbstractUpdateTicketAction<Junit5ZephyrReport.Test>
    implements Junit5Action, Junit5ActionJiraDescription {

    public Junit5UpdateTicketAction() {
        super(new Junit5TagService());
    }

    @Override
    public void process() {
        log.info("Starting Junit5 Update Ticket Action");
        Junit5ZephyrReport report = getJunit5ZephyrReport();
        if (report == null) {
            log.warn("Report is null, skipping execution creation");
            return;
        }
        report.getTests().stream()
            .filter(Objects::nonNull)
            .forEach(this::updateJiraIssue);
    }

    @Override
    public void jiraDescriptionPostProcess(Junit5ZephyrReport.Test test, StringBuilder builder) {
        Junit5ActionJiraDescription.super.jiraDescriptionPostProcess(test, builder);
    }
}

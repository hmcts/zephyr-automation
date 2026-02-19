package uk.hmcts.zephyr.automation.cypress.actions;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.actions.AbstractUpdateTicketAction;
import uk.hmcts.zephyr.automation.cypress.CypressTagService;
import uk.hmcts.zephyr.automation.cypress.models.CypressReport;

@Slf4j
public class CypressUpdateTicketAction
    extends AbstractUpdateTicketAction<CypressReport.CypressTest>
    implements CypressAction {

    public CypressUpdateTicketAction() {
        super(new CypressTagService());
    }

    @Override
    public void process() {
        log.info("Starting Cypress Update Ticket Action");
        CypressReport report = getCypressReport();
        report.getTests().forEach(this::updateJiraIssue);
    }
}

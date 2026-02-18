package uk.hmcts.zephyr.automation.cypress.actions;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.actions.AbstractCreateTicketAction;
import uk.hmcts.zephyr.automation.cypress.CypressTagService;
import uk.hmcts.zephyr.automation.cypress.models.CypressReport;
import uk.hmcts.zephyr.automation.util.FileUtil;

@Slf4j
public class CypressCreateTicketAction
    extends AbstractCreateTicketAction<CypressReport.CypressTest>
    implements CypressAction {

    public CypressCreateTicketAction() {
        super(new CypressTagService());
    }

    @Override
    public void process() {
        log.info("Starting Cypress Create Ticket Action");
        CypressReport report = getCypressReport();
        report.getTests().forEach(this::createJiraIssue);

        // Write the updated features back to the file
        FileUtil.writeToFile(Config.getReportPath(), report);
    }
}

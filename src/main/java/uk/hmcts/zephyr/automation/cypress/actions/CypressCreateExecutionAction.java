package uk.hmcts.zephyr.automation.cypress.actions;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.actions.AbstractCreateExecutionAction;
import uk.hmcts.zephyr.automation.cypress.CypressTagService;
import uk.hmcts.zephyr.automation.cypress.models.CypressReport;

@Slf4j
public class CypressCreateExecutionAction
    extends AbstractCreateExecutionAction<CypressReport.CypressTest>
    implements CypressAction {

    public CypressCreateExecutionAction() {
        super(new CypressTagService());
    }

    @Override
    public void process() {
        log.info("Starting Cypress Create Execution Action");
        CypressReport report = getCypressReport();
        processTests(report.getTests());
    }
}

package uk.hmcts.zephyr.automation.junit5.actions;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.actions.AbstractCreateExecutionAction;
import uk.hmcts.zephyr.automation.junit5.Junit5TagService;
import uk.hmcts.zephyr.automation.junit5.model.Junit5ZephyrReport;

@Slf4j
public class JUnit5CreateExecutionAction
    extends AbstractCreateExecutionAction<Junit5ZephyrReport.Test>
    implements Junit5Action {

    public JUnit5CreateExecutionAction() {
        super(new Junit5TagService());
    }

    @Override
    public void process() {
        log.info("Starting Junit5 Create Execution Action");
        Junit5ZephyrReport report = getJunit5ZephyrReport();
        if (report == null) {
            log.warn("Report is null, skipping execution creation");
            return;
        }
        processTests(report.getTests());
    }
}

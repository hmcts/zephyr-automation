package uk.hmcts.zephyr.automation.cypress.actions;


import com.fasterxml.jackson.core.type.TypeReference;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cypress.models.CypressReport;
import uk.hmcts.zephyr.automation.util.FileUtil;

public interface CypressAction {

    default CypressReport getCypressReport() {
        return FileUtil.readFromFile(Config.getReportPath(),
            new TypeReference<>() {
            });
    }
}

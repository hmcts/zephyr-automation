package uk.hmcts.zephyr.automation.cypress.actions;

import tools.jackson.core.type.TypeReference;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cypress.models.CypressReport;
import uk.hmcts.zephyr.util.FileUtil;

public interface CypressAction {

    default CypressReport getCypressReport() {
        return FileUtil.readFromFile(Config.reportPath,
            new TypeReference<>() {
            });
    }
}

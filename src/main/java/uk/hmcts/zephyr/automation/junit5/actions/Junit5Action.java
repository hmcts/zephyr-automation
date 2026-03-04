package uk.hmcts.zephyr.automation.junit5.actions;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.junit5.model.Junit5ZephyrReport;
import uk.hmcts.zephyr.automation.util.FileUtil;

public interface Junit5Action {
    default Junit5ZephyrReport getJunit5ZephyrReport() {
        return FileUtil.readFromFile(Config.getReportPath(),
            new TypeReference<>() {
            });
    }
}

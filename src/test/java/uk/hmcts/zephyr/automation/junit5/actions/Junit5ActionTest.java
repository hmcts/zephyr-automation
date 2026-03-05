package uk.hmcts.zephyr.automation.junit5.actions;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.junit5.model.Junit5ZephyrReport;
import uk.hmcts.zephyr.automation.util.FileUtil;

import static org.junit.jupiter.api.Assertions.assertSame;

class Junit5ActionTest {

    private final Junit5Action action = new Junit5Action() { };

    @Test
    void getJunit5ZephyrReport_readsReportFromConfiguredPath() {
        String reportPath = "/tmp/junit5-report.json";
        Junit5ZephyrReport expectedReport = new Junit5ZephyrReport();

        try (MockedStatic<Config> configMock = Mockito.mockStatic(Config.class);
             MockedStatic<FileUtil> fileUtilMock = Mockito.mockStatic(FileUtil.class)) {

            configMock.when(Config::getReportPath).thenReturn(reportPath);
            fileUtilMock.when(() -> FileUtil.readFromFile(Mockito.eq(reportPath),
                Mockito.<TypeReference<Junit5ZephyrReport>>any()))
                .thenReturn(expectedReport);

            Junit5ZephyrReport actualReport = action.getJunit5ZephyrReport();

            assertSame(expectedReport, actualReport);
            configMock.verify(Config::getReportPath);
            fileUtilMock.verify(() -> FileUtil.readFromFile(Mockito.eq(reportPath),
                Mockito.<TypeReference<Junit5ZephyrReport>>any()));
        }
    }
}

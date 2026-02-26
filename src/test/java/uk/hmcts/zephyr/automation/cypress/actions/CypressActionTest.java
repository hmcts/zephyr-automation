package uk.hmcts.zephyr.automation.cypress.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cypress.models.CypressReport;
import uk.hmcts.zephyr.automation.support.TestUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CypressActionTest {

    private final CypressAction action = new CypressAction() { };

    @AfterEach
    void tearDown() throws Exception {
        TestUtil.resetSingletons();
    }

    @Nested
    class GetCypressReportTest {

        @TempDir
        Path tempDir;

        @Test
        void given_existingReport_when_getCypressReport_then_returnsParsedReport() throws Exception {
            Path reportFile = tempDir.resolve("cypress-report.json");
            CypressReport expectedReport = sampleReport();
            new ObjectMapper().writeValue(reportFile.toFile(), expectedReport);

            instantiateConfig(reportFile.toString());

            CypressReport actualReport = action.getCypressReport();

            assertEquals(expectedReport, actualReport);
        }

        @Test
        void given_missingReport_when_getCypressReport_then_throwsException() throws Exception {
            Path reportFile = tempDir.resolve("missing-report.json");

            instantiateConfig(reportFile.toString());

            IOException exception = assertThrows(IOException.class, action::getCypressReport);
            assertTrue(exception.getMessage().contains(reportFile.getFileName().toString()));
        }

        private void instantiateConfig(String reportPath) throws Exception {
            TestUtil.resetSingletons();
            Config.instantiate(argsWithReportPath(reportPath));
        }

        private CypressReport sampleReport() {
            final CypressReport report = new CypressReport();
            CypressReport.CypressTest test = new CypressReport.CypressTest();
            test.setTitle("should upload report");
            test.setFullTitle("suite should upload report");
            test.setParents(List.of("suite", "scenario"));
            test.setFile("tests/sample.cy.js");
            test.setStatus("passed");
            report.setTests(List.of(test));
            return report;
        }

        private String[] argsWithReportPath(String reportPath) {
            String[] args = TestUtil.defaultArgs(
                Config.ActionType.CREATE_TICKETS, Config.ProcessType.CYPRESS_JSON_REPORT);
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("report-path=")) {
                    args[i] = "report-path=" + reportPath;
                    return args;
                }
            }
            throw new IllegalStateException("report-path argument missing");
        }
    }
}


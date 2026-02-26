package uk.hmcts.zephyr.automation.cucumber.actions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element;
import uk.hmcts.zephyr.automation.support.CucumberDataUtil;
import uk.hmcts.zephyr.automation.support.TestUtil;
import uk.hmcts.zephyr.automation.util.FileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class CucumberCreateTicketActionTest {

    private MockedStatic<FileUtil> fileUtilMock;

    @BeforeEach
    void setUp() throws Exception {
        fileUtilMock = mockStatic(FileUtil.class);
        TestUtil.resetSingletons();
        Config.instantiate(TestUtil.defaultArgs(Config.ActionType.CREATE_TICKETS, Config.ProcessType.CUCUMBER_JSON_REPORT));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (fileUtilMock != null) {
            fileUtilMock.close();
            fileUtilMock = null;
        }
        TestUtil.resetSingletons();
    }

    @Nested
    class ProcessTest {

        @Test
        void given_featuresWithScenarios_when_process_then_createsTicketsAndWritesReport() {
            CucumberCreateTicketAction action = spy(new CucumberCreateTicketAction());
            Element firstScenario = CucumberDataUtil.scenario("first-scenario");
            Element secondScenario = CucumberDataUtil.scenario("second-scenario");
            CucumberFeature feature = CucumberDataUtil.featureWithElements(firstScenario, secondScenario);
            List<CucumberFeature> features = new ArrayList<>(List.of(feature));

            doReturn(features).when(action).getFeatures();
            doReturn(Optional.empty()).when(action).createJiraIssue(any());

            action.process();

            verify(action).getFeatures();
            verify(action).createJiraIssue(firstScenario);
            verify(action).createJiraIssue(secondScenario);
            fileUtilMock.verify(() -> FileUtil.writeToFile(Config.getReportPath(), features));
        }

        @Test
        void given_featureWithoutElements_when_process_then_skipsCreationButWritesReport() {
            CucumberCreateTicketAction action = spy(new CucumberCreateTicketAction());
            CucumberFeature feature = new CucumberFeature();
            feature.setName("Empty feature");
            feature.setElements(null);
            List<CucumberFeature> features = new ArrayList<>(List.of(feature));

            doReturn(features).when(action).getFeatures();

            action.process();

            verify(action).getFeatures();
            verify(action, never()).createJiraIssue(any());
            fileUtilMock.verify(() -> FileUtil.writeToFile(Config.getReportPath(), features));
        }
    }
}


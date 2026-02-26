package uk.hmcts.zephyr.automation.cucumber.actions;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature;
import uk.hmcts.zephyr.automation.support.TestUtil;
import uk.hmcts.zephyr.automation.util.FileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

class CucumberActionTest {

    private MockedStatic<FileUtil> fileUtilMock;

    private final CucumberAction action = new CucumberAction() { };

    @BeforeEach
    void setUp() throws Exception {
        fileUtilMock = mockStatic(FileUtil.class);
        TestUtil.resetSingletons();
        Config.instantiate(TestUtil.defaultArgs(
            Config.ActionType.CREATE_TICKETS, Config.ProcessType.CUCUMBER_JSON_REPORT));
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
    class GetFeaturesTest {

        @Test
        void given_featuresWithScenarios_when_getFeatures_then_setsBackReferenceAndReturnsList() {
            CucumberFeature feature = new CucumberFeature();
            feature.setName("Login feature");
            CucumberFeature.Element scenario = new CucumberFeature.Element();
            scenario.setName("Successful login");
            scenario.setType("scenario");
            List<CucumberFeature.Element> elements = new ArrayList<>();
            elements.add(scenario);
            elements.add(null);
            feature.setElements(elements);
            List<CucumberFeature> cucumberFeatures = new ArrayList<>();
            cucumberFeatures.add(feature);
            cucumberFeatures.add(null);

            fileUtilMock.when(() -> FileUtil.readFromFile(
                eq(Config.getReportPath()),
                anyTypeReference()
            )).thenReturn(cucumberFeatures);

            List<CucumberFeature> result = action.getFeatures();

            assertSame(cucumberFeatures, result);
            assertSame(feature, scenario.getCucumberFeature(), "Scenario should reference its feature");
        }
    }

    @Nested
    class GetElementFilterTest {

        @Test
        void given_scenarioElement_when_filterApplied_then_returnsTrue() {
            Predicate<CucumberFeature.Element> predicate = action.getElementFilter();
            CucumberFeature.Element scenario = new CucumberFeature.Element();
            scenario.setType("scenario");

            assertTrue(predicate.test(scenario));
        }

        @Test
        void given_nonScenarioElement_when_filterApplied_then_returnsFalse() {
            Predicate<CucumberFeature.Element> predicate = action.getElementFilter();
            CucumberFeature.Element background = new CucumberFeature.Element();
            background.setType("background");

            assertFalse(predicate.test(background));
        }
    }

    private static TypeReference<List<CucumberFeature>> anyTypeReference() {
        //Mockito does not retain generic type info on matchers, so suppressing unchecked warning here
        @SuppressWarnings("unchecked")
        TypeReference<List<CucumberFeature>> typeReference =
            (TypeReference<List<CucumberFeature>>) any(TypeReference.class);
        return typeReference;
    }
}

package uk.hmcts.zephyr.automation.cucumber.actions;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element;
import uk.hmcts.zephyr.automation.util.FileUtil;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public interface CucumberAction {

    default List<CucumberFeature> getFeatures() {
        List<CucumberFeature> cucumberFeatures = FileUtil.readFromFile(Config.getReportPath(),
            new TypeReference<>() {
            });
        cucumberFeatures.stream()
            .filter(Objects::nonNull)
            .forEach(feature -> feature.getElements()
                .stream()
                .filter(Objects::nonNull)
                .forEach(scenario -> scenario.setCucumberFeature(feature)));
        return cucumberFeatures;
    }

    default Predicate<Element> getElementFilter() {
        //Currently only supporting creating JIRA issues for scenarios
        //this can be extended to support other element types if needed
        return element -> element.getType().equals("scenario");
    }
}

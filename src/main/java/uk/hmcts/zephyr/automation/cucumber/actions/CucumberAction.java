package uk.hmcts.zephyr.automation.cucumber.actions;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cucumber.models.Element;
import uk.hmcts.zephyr.automation.cucumber.models.Feature;
import uk.hmcts.zephyr.automation.util.FileUtil;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public interface CucumberAction {

    default List<Feature> getFeatures() {
        List<Feature> features = FileUtil.readFromFile(Config.getReportPath(),
            new TypeReference<>() {
            });
        features.stream()
            .filter(Objects::nonNull)
            .forEach(feature -> feature.getElements()
                .stream()
                .filter(Objects::nonNull)
                .forEach(scenario -> scenario.setFeature(feature)));
        return features;
    }

    default Predicate<Element> getElementFilter() {
        //Currently only supporting creating JIRA issues for scenarios
        //this can be extended to support other element types if needed
        return element -> element.getType().equals("scenario");
    }
}

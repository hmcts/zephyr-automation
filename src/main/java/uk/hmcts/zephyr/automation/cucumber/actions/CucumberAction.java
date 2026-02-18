package uk.hmcts.zephyr.automation.cucumber.actions;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cucumber.models.Feature;
import uk.hmcts.zephyr.util.FileUtil;

import java.util.List;
import java.util.Objects;

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
}

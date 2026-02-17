package uk.hmcts.zephyr.automation.actions.cucumber;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.actions.Action;
import uk.hmcts.zephyr.automation.cucumber.report.Feature;
import uk.hmcts.zephyr.util.FileUtil;

import java.util.List;

@Slf4j
public abstract class AbstractCucumberAction implements Action {

    protected AbstractCucumberAction(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("cucumber-path=")) {
                Config.cucumberPath = arg.substring("cucumber-path=".length());
            }
        }
        if (Config.cucumberPath == null) {
            throw new IllegalArgumentException(
                "For CREATE_TICKETS action type, cucumber-path must be specified as a command line "
                    + "argument");
        }
    }

    protected List<Feature> getFeatures() {
        log.info("Reading features from file: {}", Config.cucumberPath);
        List<Feature> features = FileUtil.readFromFile(Config.cucumberPath,
            new TypeReference<>() {
            });
        log.info("Features read: {}", features.size());
        return features;
    }

}

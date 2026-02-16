package uk.hmcts.zephyr.util;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cucumber.report.Element;
import uk.hmcts.zephyr.automation.cucumber.report.Feature;
import uk.hmcts.zephyr.automation.cucumber.report.Location;
import uk.hmcts.zephyr.automation.cucumber.report.Step;
import uk.hmcts.zephyr.automation.cucumber.report.Tag;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Slf4j
public class TagUtil {

    public static final String JIRA_KEY_TAG_PREFIX = "@JIRA-KEY:";

    public static Optional<String> extractJiraKeyFromTag(Element scenario) {
        return extractTagWithPrefix(scenario, JIRA_KEY_TAG_PREFIX)
            .map(key -> key.replace(JIRA_KEY_TAG_PREFIX, ""));
    }

    public static Optional<String> extractTagWithPrefix(Element scenario, String prefix) {
        return scenario.getTags().stream()
            .filter(tag -> tag.getName().startsWith(prefix))
            .map(tag -> tag.getName().substring(prefix.length()))
            .findFirst();
    }

    public static void addTag(Feature feature, Element scenario, String tagName) {
        log.info("Adding tag '{}' to scenario '{}' in feature '{}'", tagName, scenario.getName(), feature.getUri());
        boolean alreadyHasTag = scenario.hasTag(tagName);
        if (alreadyHasTag) {
            log.info("Scenario '{}' already has tag '{}', skipping addition.", scenario.getName(), tagName);
            return;
        }
        //Update the feature file with the tag
        Location tagLocation = addTagToScenario(feature, scenario, tagName);
        // Add the tag to the scenario in memory
        scenario.addTag(new Tag(tagName, "Tag", tagLocation));
        log.info("Tag '{}' added to scenario '{}' at line {} in feature '{}'",
            tagName, scenario.getName(), tagLocation.getLine(), feature.getUri());
    }

    private static Location addTagToScenario(Feature feature, Element scenario, String tagName) {
        String featureFilePath = resolveFeatureFilePath(feature.getUri());

        try {
            List<String> lines = Files.readAllLines(Paths.get(featureFilePath));
            //Cucumber line numbers are 1-based, but List is 0-based
            int scenarioLine = scenario.getLine() - 1;

            int tagLine = scenarioLine;
            int tagColumn = 1;

            if (lines.get(tagLine - 1).trim().startsWith("@")) {
                tagLine = scenarioLine - 1;
                String prefix = lines.get(tagLine) + " ";
                tagColumn = tagColumn + prefix.length();
                lines.set(tagLine, prefix + tagName);
            } else {
                //Get number of spaces at the start of the scenario line to maintain indentation
                String scenarioLineContent = lines.get(scenarioLine);
                String indentation =
                    scenarioLineContent.substring(0, scenarioLineContent.indexOf(scenarioLineContent.trim()));
                //Insert the tag line on the scenario line (Forcing the scenario line to go down by one line)
                lines.add(scenarioLine, indentation + tagName);
                //Because I have added a line above the scenario, I need to update the line numbers of the scenario and all tags in the scenario
                updateLineNumbersOnFeature(feature, tagLine);
                tagColumn = tagColumn + indentation.length();
            }
            //Write the updated lines back to the feature file
            Files.write(Paths.get(featureFilePath), lines);

            //Return the line number of the added tag (Cucumber line numbers are 1-based)
            return new Location(tagLine + 1, tagColumn);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update feature file '" + featureFilePath, e);
        }
    }

    private static void updateLineNumbersOnFeature(Feature feature, int tagLine) {
        if (feature.getElements() != null) {
            for (Element scenario : feature.getElements()) {
                updateLineNumbersOnScenario(scenario, tagLine);
            }
        }
        if (feature.getTags() != null) {
            feature.getTags().forEach(tag -> updateLineNumbersOnScenarioTag(tag, tagLine));
        }
    }

    private static void updateLineNumbersOnScenario(Element scenario, int tagLine) {
        if (scenario.getLine() >= tagLine) {
            scenario.setLine(scenario.getLine() + 1);

            if (scenario.getTags() != null) {
                scenario.getTags().forEach(tag -> updateLineNumbersOnScenarioTag(tag, tagLine));
            }
            if (scenario.getSteps() != null) {
                for (Step step : scenario.getSteps()) {
                    if (step.getLine() >= tagLine) {
                        step.setLine(step.getLine() + 1);
                    }
                }
            }
        }
    }

    private static void updateLineNumbersOnScenarioTag(Tag tag, int tagLine) {
        if (tag.getLocation() == null) {
            return;
        }
        if (tag.getLocation().getLine() >= tagLine) {
            tag.getLocation().setLine(tag.getLocation().getLine() + 1);
        }
    }


    private static String resolveFeatureFilePath(String uri) {
        if (uri == null) {
            throw new RuntimeException("Feature file path is null");
        }
        String prefix = "classpath:";
        if (uri.startsWith(prefix)) {
            String relativePath = uri.substring(prefix.length());
            String base = Config.basePath + "/resources/";
            return base + relativePath;
        }
        throw new RuntimeException("Unsupported URI format: " + uri);
    }
}

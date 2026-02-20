package uk.hmcts.zephyr.automation.cucumber;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cucumber.models.Element;
import uk.hmcts.zephyr.automation.cucumber.models.Feature;
import uk.hmcts.zephyr.automation.cucumber.models.Location;
import uk.hmcts.zephyr.automation.cucumber.models.Step;
import uk.hmcts.zephyr.automation.cucumber.models.Tag;
import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.util.FileUtil;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static uk.hmcts.zephyr.automation.jira.JiraConfig.JIRA_KEY_TAG_PREFIX;

@Slf4j
public class CucumberTagService implements TagService<Element> {


    private String addTagPrefix(String tagName) {
        return "@" + tagName;
    }

    @Override
    public Optional<String> extractJiraKeyFromTag(Element scenario) {
        return extractTagWithPrefix(scenario, JIRA_KEY_TAG_PREFIX)
            .map(key -> key.replace(addTagPrefix(JIRA_KEY_TAG_PREFIX), ""));
    }

    @Override
    public Optional<String> extractTagWithPrefix(Element scenario, String prefix) {
        return extractTagListWithPrefix(scenario, prefix).stream().findFirst();
    }

    @Override
    public List<String> extractTagListWithPrefix(Element scenario, String prefix) {
        return scenario.getTags().stream()
            .filter(tag -> tag.getName().startsWith(addTagPrefix(prefix)))
            .map(tag -> tag.getName().substring(addTagPrefix(prefix).length()))
            .toList();
    }

    @Override
    public void addTag(Element scenario, String tagNameBase) {
        String tagName = addTagPrefix(tagNameBase);
        Feature feature = scenario.getFeature();
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

    private Location addTagToScenario(Feature feature, Element scenario, String tagName) {
        String featureFilePath = resolveFeatureFilePath(feature.getUri());

        try {
            //Cucumber line numbers are 1-based, but List is 0-based
            List<String> lines = FileUtil.readFileAsLines(featureFilePath);
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
                //Update line numbers on the feature to account for the new line
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

    private void updateLineNumbersOnFeature(Feature feature, int tagLine) {
        if (feature.getElements() != null) {
            for (Element scenario : feature.getElements()) {
                updateLineNumbersOnScenario(scenario, tagLine);
            }
        }
        if (feature.getTags() != null) {
            feature.getTags().forEach(tag -> updateLineNumbersOnScenarioTag(tag, tagLine));
        }
    }

    private void updateLineNumbersOnScenario(Element scenario, int tagLine) {
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

    private void updateLineNumbersOnScenarioTag(Tag tag, int tagLine) {
        if (tag.getLocation() == null) {
            return;
        }
        if (tag.getLocation().getLine() >= tagLine) {
            tag.getLocation().setLine(tag.getLocation().getLine() + 1);
        }
    }


    private String resolveFeatureFilePath(String uri) {
        if (uri == null) {
            throw new RuntimeException("Feature file path is null");
        }
        String prefix = "classpath:";
        if (uri.startsWith(prefix)) {
            String relativePath = uri.substring(prefix.length());
            String base = Config.getBasePath() + "/resources/";
            return base + relativePath;
        }
        throw new RuntimeException("Unsupported URI format: " + uri);
    }
}

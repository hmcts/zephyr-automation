package uk.hmcts.zephyr.automation.cucumber;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.TestTag;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Location;
import uk.hmcts.zephyr.automation.util.FileUtil;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class CucumberTagService implements TagService<Element> {

    @Override
    public List<TestTag> extractTagListFromType(Element scenario, TestTag.Type tagType) {
        String prefix = getTagPrefix(tagType);
        return scenario.getTags().stream()
            .filter(tag -> tag.getName().startsWith(prefix))
            .map(tag -> tag.getName().substring(prefix.length()))
            .map(value -> new TestTag(tagType, value))
            .toList();
    }

    @Override
    public void addTag(Element scenario, TestTag testTag) {
        String tagName = getTagPrefix(testTag.type());
        if (testTag.value() != null) {
            tagName = tagName + testTag.value();
        }
        CucumberFeature cucumberFeature = scenario.getCucumberFeature();
        log.info("Adding tag '{}' to scenario '{}' in feature '{}'",
            tagName, scenario.getName(), cucumberFeature.getUri());
        boolean alreadyHasTag = scenario.hasTag(tagName);
        if (alreadyHasTag) {
            log.info("Scenario '{}' already has tag '{}', skipping addition.", scenario.getName(), tagName);
            return;
        }
        //Update the feature file with the tag
        Location tagLocation = addTagToScenario(cucumberFeature, scenario, tagName);
        // Add the tag to the scenario in memory
        scenario.addTag(new CucumberFeature.Tag(tagName, "Tag", tagLocation));
        log.info("Tag '{}' added to scenario '{}' at line {} in feature '{}'",
            tagName, scenario.getName(), tagLocation.getLine(), cucumberFeature.getUri());
    }

    private Location addTagToScenario(CucumberFeature cucumberFeature, Element scenario, String tagName) {
        String featureFilePath = resolveFeatureFilePath(cucumberFeature.getUri());

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
                updateLineNumbersOnFeature(cucumberFeature, tagLine);
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

    private void updateLineNumbersOnFeature(CucumberFeature cucumberFeature, int tagLine) {
        if (cucumberFeature.getElements() != null) {
            for (Element scenario : cucumberFeature.getElements()) {
                updateLineNumbersOnScenario(scenario, tagLine);
            }
        }
        if (cucumberFeature.getTags() != null) {
            cucumberFeature.getTags().forEach(tag -> updateLineNumbersOnScenarioTag(tag, tagLine));
        }
    }

    private void updateLineNumbersOnScenario(Element scenario, int tagLine) {
        if (scenario.getLine() >= tagLine) {
            scenario.setLine(scenario.getLine() + 1);

            if (scenario.getTags() != null) {
                scenario.getTags().forEach(tag -> updateLineNumbersOnScenarioTag(tag, tagLine));
            }
            if (scenario.getSteps() != null) {
                for (CucumberFeature.Element.Step step : scenario.getSteps()) {
                    if (step.getLine() >= tagLine) {
                        step.setLine(step.getLine() + 1);
                    }
                }
            }
        }
    }

    private void updateLineNumbersOnScenarioTag(CucumberFeature.Tag tag, int tagLine) {
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
        String relativePath = uri.startsWith(prefix) ? uri.substring(prefix.length()) : uri;
        String base = Config.getBasePath();
        return base + "/" + relativePath;
    }
}

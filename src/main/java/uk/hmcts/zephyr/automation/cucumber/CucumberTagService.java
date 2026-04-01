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
            List<String> lines = FileUtil.readFileAsLines(featureFilePath);
            int scenarioLineIndex = scenario.getLine() - 1;

            Location location;
            if (isExampleRowLine(lines, scenarioLineIndex)) {
                location = handleScenarioOutlineTagInsertion(lines, cucumberFeature, scenarioLineIndex, tagName);
            } else {
                location = handleStandardScenarioTagInsertion(lines, cucumberFeature, scenarioLineIndex, tagName);
            }

            Files.write(Paths.get(featureFilePath), lines);
            return location;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update feature file '" + featureFilePath, e);
        }
    }

    private Location handleScenarioOutlineTagInsertion(List<String> lines, CucumberFeature cucumberFeature,
                                                       int scenarioLineIndex, String tagName) {
        Integer examplesLineIndex = findExamplesLineIndex(lines, scenarioLineIndex);
        if (examplesLineIndex == null) {
            return handleStandardScenarioTagInsertion(lines, cucumberFeature, scenarioLineIndex, tagName);
        }

        splitExamplesBlockAfterSelectedRow(lines, cucumberFeature, examplesLineIndex, scenarioLineIndex);

        Integer existingTagLineIndex = findExampleTagLineIndex(lines, examplesLineIndex);
        if (existingTagLineIndex != null) {
            return appendTagToLine(lines, existingTagLineIndex, tagName);
        }

        String indentation = extractIndentation(lines.get(examplesLineIndex));
        lines.add(examplesLineIndex, indentation + tagName);
        updateLineNumbersOnFeature(cucumberFeature, examplesLineIndex);
        return new Location(examplesLineIndex + 1, indentation.length() + 1);
    }

    private void splitExamplesBlockAfterSelectedRow(List<String> lines, CucumberFeature cucumberFeature,
                                                    int examplesLineIndex, int scenarioLineIndex) {
        int headerLineIndex = findExampleTableHeaderLineIndex(lines, examplesLineIndex);
        if (headerLineIndex == -1 || scenarioLineIndex <= headerLineIndex) {
            return;
        }

        int blockEndExclusive = findExampleTableBlockEnd(lines, headerLineIndex + 1);
        if (scenarioLineIndex + 1 >= blockEndExclusive) {
            return;
        }

        final List<String> rowsAfterSelected =
            lines.subList(scenarioLineIndex + 1, blockEndExclusive).stream().toList();
        lines.subList(scenarioLineIndex + 1, blockEndExclusive).clear();

        int insertionIndex = scenarioLineIndex + 1;
        String examplesIndentation = extractIndentation(lines.get(examplesLineIndex));
        String headerLine = lines.get(headerLineIndex);
        lines.add(insertionIndex++, examplesIndentation + "Examples:");
        lines.add(insertionIndex++, headerLine);
        for (String row : rowsAfterSelected) {
            lines.add(insertionIndex++, row);
        }

        // Net line increase is +2 (rows are moved, not duplicated), so update model line numbers accordingly.
        updateLineNumbersOnFeature(cucumberFeature, scenarioLineIndex + 1);
        updateLineNumbersOnFeature(cucumberFeature, scenarioLineIndex + 1);
    }

    private int findExampleTableHeaderLineIndex(List<String> lines, int examplesLineIndex) {
        for (int i = examplesLineIndex + 1; i < lines.size(); i++) {
            String trimmed = lines.get(i).trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("|")) {
                return i;
            }
            return -1;
        }
        return -1;
    }

    private int findExampleTableBlockEnd(List<String> lines, int startIndex) {
        for (int i = startIndex; i < lines.size(); i++) {
            String trimmed = lines.get(i).trim();
            if (trimmed.startsWith("|")) {
                continue;
            }
            return i;
        }
        return lines.size();
    }

    private Location handleStandardScenarioTagInsertion(List<String> lines, CucumberFeature cucumberFeature,
                                                        int scenarioLineIndex, String tagName) {
        int tagLineIndex = scenarioLineIndex;
        int tagColumn = 1;

        if (scenarioLineIndex > 0 && lines.get(tagLineIndex - 1).trim().startsWith("@")) {
            tagLineIndex = scenarioLineIndex - 1;
            String prefix = lines.get(tagLineIndex) + " ";
            tagColumn = tagColumn + prefix.length();
            lines.set(tagLineIndex, prefix + tagName);
        } else {
            String scenarioLineContent = lines.get(scenarioLineIndex);
            String indentation = extractIndentation(scenarioLineContent);
            lines.add(scenarioLineIndex, indentation + tagName);
            updateLineNumbersOnFeature(cucumberFeature, tagLineIndex);
            tagColumn = tagColumn + indentation.length();
        }

        return new Location(tagLineIndex + 1, tagColumn);
    }

    private Location appendTagToLine(List<String> lines, int tagLineIndex, String tagName) {
        String prefix = lines.get(tagLineIndex) + " ";
        lines.set(tagLineIndex, prefix + tagName);
        return new Location(tagLineIndex + 1, prefix.length() + 1);
    }

    private boolean isExampleRowLine(List<String> lines, int lineIndex) {
        if (lineIndex < 0 || lineIndex >= lines.size()) {
            return false;
        }
        return lines.get(lineIndex).trim().startsWith("|");
    }

    private Integer findExamplesLineIndex(List<String> lines, int fromLineIndex) {
        for (int i = fromLineIndex; i >= 0; i--) {
            String trimmed = lines.get(i).trim();
            if (trimmed.startsWith("Examples")) {
                return i;
            }
            if (trimmed.isEmpty() || trimmed.startsWith("|") || trimmed.startsWith("@")) {
                continue;
            }
            break;
        }
        return null;
    }

    private Integer findExampleTagLineIndex(List<String> lines, int examplesLineIndex) {
        for (int i = examplesLineIndex - 1; i >= 0; i--) {
            String trimmed = lines.get(i).trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("@")) {
                return i;
            }
            break;
        }
        return null;
    }

    private String extractIndentation(String lineContent) {
        int index = 0;
        while (index < lineContent.length() && Character.isWhitespace(lineContent.charAt(index))) {
            index++;
        }
        return lineContent.substring(0, index);
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

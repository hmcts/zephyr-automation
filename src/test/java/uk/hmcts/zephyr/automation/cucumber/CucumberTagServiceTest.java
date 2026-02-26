package uk.hmcts.zephyr.automation.cucumber;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element.Step;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Location;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Result;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Tag;
import uk.hmcts.zephyr.automation.support.CucumberDataUtil;
import uk.hmcts.zephyr.automation.support.TestUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.hmcts.zephyr.automation.jira.JiraConfig.JIRA_KEY_TAG_PREFIX;
import static uk.hmcts.zephyr.automation.jira.JiraConfig.JIRA_LABEL_TAG_PREFIX;

class CucumberTagServiceTest {

    @TempDir
    Path tempDir;

    private CucumberTagService tagService;
    private Element scenario;
    private CucumberFeature feature;
    private Path featurePath;

    @BeforeEach
    void setUp() throws Exception {
        TestUtil.resetSingletons();
        Config.instantiate(argsWithBasePath(tempDir));
        tagService = new CucumberTagService();

        feature = new CucumberFeature();
        feature.setName("Payments feature");
        feature.setUri("classpath:features/payments.feature");

        scenario = new Element();
        scenario.setName("User pays invoice");
        scenario.setCucumberFeature(feature);
        scenario.setTags(new ArrayList<>());
        scenario.setSteps(new ArrayList<>());
        feature.setElements(new ArrayList<>(List.of(scenario)));

        featurePath = tempDir.resolve("features/payments.feature");
        Files.createDirectories(featurePath.getParent());
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtil.resetSingletons();
    }

    @Nested
    class ExtractJiraKeyFromTagTest {

        @Test
        void givenTagContainingJiraKey_whenExtract_thenReturnsKey() {
            scenario.addTag(CucumberDataUtil.tag("@" + JIRA_KEY_TAG_PREFIX + "ABC-321"));

            Optional<String> jiraKey = tagService.extractJiraKeyFromTag(scenario);

            assertTrue(jiraKey.isPresent());
            assertEquals("ABC-321", jiraKey.get());
        }

        @Test
        void givenNoMatchingTag_whenExtract_thenReturnsEmpty() {
            assertTrue(tagService.extractJiraKeyFromTag(scenario).isEmpty());
        }
    }

    @Nested
    class ExtractTagWithPrefixTest {

        @Test
        void givenMatchingTag_whenExtract_thenReturnsSuffix() {
            scenario.addTag(CucumberDataUtil.tag("@" + JIRA_LABEL_TAG_PREFIX + "critical"));

            Optional<String> result = tagService.extractTagWithPrefix(scenario, JIRA_LABEL_TAG_PREFIX);

            assertTrue(result.isPresent());
            assertEquals("critical", result.get());
        }

        @Test
        void givenNoMatchingTag_whenExtract_thenReturnsEmpty() {
            assertTrue(tagService.extractTagWithPrefix(scenario, JIRA_LABEL_TAG_PREFIX).isEmpty());
        }
    }

    @Nested
    class ExtractTagListWithPrefixTest {

        @Test
        void givenMultipleMatches_whenExtract_thenReturnsAllSuffixes() {
            scenario.addTag(CucumberDataUtil.tag("@" + JIRA_LABEL_TAG_PREFIX + "critical"));
            scenario.addTag(CucumberDataUtil.tag("@" + JIRA_LABEL_TAG_PREFIX + "regression"));
            scenario.addTag(CucumberDataUtil.tag("@otherprefixvalue"));

            List<String> extracted = tagService.extractTagListWithPrefix(scenario, JIRA_LABEL_TAG_PREFIX);

            assertIterableEquals(List.of("critical", "regression"), extracted);
        }
    }

    @Nested
    class AddTagTest {

        @Test
        void givenScenarioWithoutExistingTagLine_whenAddTag_thenInsertsNewLineAndUpdatesModel() throws IOException {
            scenario.setLine(2);
            scenario.setSteps(new ArrayList<>(List.of(stepAtLine(3, "passed"))));
            copyFixture("add-tag-without-existing.before.feature");

            String tagSuffix = JIRA_KEY_TAG_PREFIX + "ABC-900";
            tagService.addTag(scenario, tagSuffix);

            assertIterableEquals(readFixture("add-tag-without-existing.after.feature"), Files.readAllLines(featurePath));
            assertEquals(3, scenario.getLine());
            assertEquals(4, scenario.getSteps().get(0).getLine());

            Tag addedTag = scenario.getTags().stream()
                .filter(t -> t.getName().equals("@" + tagSuffix))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected tag to be added"));
            assertEquals(2, addedTag.getLocation().getLine());
            assertEquals(3, addedTag.getLocation().getColumn());
        }

        @Test
        void givenScenarioWithExistingTagLine_whenAddTag_thenAppendsToTagLine() throws IOException {
            scenario.setLine(3);
            scenario.setSteps(new ArrayList<>(List.of(stepAtLine(4, "passed"))));
            copyFixture("add-tag-existing-line.before.feature");
            scenario.addTag(new Tag("@existing", "Tag", new Location(2, 3)));

            String tagSuffix = JIRA_KEY_TAG_PREFIX + "ABC-901";
            tagService.addTag(scenario, tagSuffix);

            assertIterableEquals(readFixture("add-tag-existing-line.after.feature"), Files.readAllLines(featurePath));
            assertEquals(3, scenario.getLine());
            assertEquals(4, scenario.getSteps().get(0).getLine());

            Tag addedTag = scenario.getTags().stream()
                .filter(t -> t.getName().equals("@" + tagSuffix))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected tag to be added"));
            assertEquals(2, addedTag.getLocation().getLine());
            assertEquals(13, addedTag.getLocation().getColumn());
        }

        @Test
        void givenScenarioWithOtherTags_whenAddTag_thenExistingTagLineNumbersShiftDown() throws IOException {
            scenario.setLine(2);
            scenario.setSteps(new ArrayList<>(List.of(stepAtLine(3, "passed"))));
            copyFixture("add-tag-shift-existing.before.feature");
            Tag existingTag = new Tag("@existing", "Tag", new Location(2, 3));
            scenario.getTags().add(existingTag);

            String tagSuffix = JIRA_KEY_TAG_PREFIX + "ABC-902";
            tagService.addTag(scenario, tagSuffix);

            assertIterableEquals(readFixture("add-tag-shift-existing.after.feature"), Files.readAllLines(featurePath));
            Tag shiftedTag = scenario.getTags().stream()
                .filter(t -> t.getName().equals("@existing"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected existing tag to remain"));
            assertEquals(3, shiftedTag.getLocation().getLine());
            assertEquals(3, shiftedTag.getLocation().getColumn());
        }

        @Test
        void givenScenarioAlreadyContainingTag_whenAddTag_thenSkipsFileUpdate() throws IOException {
            scenario.setLine(2);
            copyFixture("add-tag-skip.before.feature");
            String tagSuffix = JIRA_KEY_TAG_PREFIX + "ABC-999";
            scenario.addTag(new Tag("@" + tagSuffix, "Tag", new Location(2, 3)));
            List<String> original = Files.readAllLines(featurePath);

            tagService.addTag(scenario, tagSuffix);

            assertEquals(1, scenario.getTags().stream()
                .filter(t -> t.getName().equals("@" + tagSuffix))
                .count());
            assertIterableEquals(original, Files.readAllLines(featurePath));
        }
    }

    private void copyFixture(String resourceName) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/cucumber/tag-service/" + resourceName)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing fixture: " + resourceName);
            }
            Files.createDirectories(featurePath.getParent());
            Files.copy(inputStream, featurePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private List<String> readFixture(String resourceName) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/cucumber/tag-service/" + resourceName)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing fixture: " + resourceName);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
        }
    }

    private Step stepAtLine(int line, String status) {
        Step step = new Step();
        step.setLine(line);
        Result result = new Result();
        result.setStatus(status);
        step.setResult(result);
        return step;
    }

    private String[] argsWithBasePath(Path basePath) {
        String[] args = TestUtil.defaultArgs(Config.ActionType.CREATE_TICKETS, Config.ProcessType.CUCUMBER_JSON_REPORT);
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("base-path=")) {
                args[i] = "base-path=" + basePath;
            }
        }
        return args;
    }
}

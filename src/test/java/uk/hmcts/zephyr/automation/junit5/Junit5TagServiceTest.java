package uk.hmcts.zephyr.automation.junit5;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.TestTag;
import uk.hmcts.zephyr.automation.junit5.model.Junit5ZephyrReport;
import uk.hmcts.zephyr.automation.support.TestUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Junit5TagServiceTest {

    private static final Path TEMPLATE_ROOT = Paths.get("src/test/resources/Junit5/template");
    private static final Path EXPECTED_ROOT = Paths.get("src/test/resources/Junit5/expected");
    private static final Path NESTED_RELATIVE_PATH = Path.of("uk/hmcts/zephyr/automation/util/SampleNestedTest.java");
    private static final Path IGNORE_RELATIVE_PATH = Path.of("uk/hmcts/zephyr/automation/util/SampleIgnoreTest.java");

    @TempDir
    Path tempDir;

    private Junit5TagService tagService;

    @BeforeEach
    void setUp() throws Exception {
        TestUtil.resetSingletons();
        Config.instantiate(TestUtil.defaultArgs(
            Config.ActionType.CREATE_TICKETS,
            Config.ProcessType.JUNIT5_JSON_REPORT,
            tempDir.toString()
        ));
        tagService = new Junit5TagService();
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtil.resetSingletons();
    }

    @Test
    void givenNestedClass_whenAddJiraKeyTag_thenMatchesExpectedFixture() throws Exception {
        copyTemplateToTemp(NESTED_RELATIVE_PATH);

        Junit5ZephyrReport.Test junitTest = junitTest(
            "uk.hmcts.zephyr.automation.util.SampleNestedTest$Nested1$Nested2",
            "targetMethod"
        );

        tagService.addTag(junitTest, new TestTag(TestTag.Type.JIRA_KEY, "ABC-123"));

        assertMatchesExpected(NESTED_RELATIVE_PATH);
    }

    @Test
    void givenWildcardImport_whenAddJiraIgnoreTag_thenMatchesExpectedFixture() throws Exception {
        copyTemplateToTemp(IGNORE_RELATIVE_PATH);

        Junit5ZephyrReport.Test junitTest = junitTest(
            "uk.hmcts.zephyr.automation.util.SampleIgnoreTest",
            "shouldSkip"
        );

        tagService.addTag(junitTest, new TestTag(TestTag.Type.JIRA_IGNORE, null));

        assertMatchesExpected(IGNORE_RELATIVE_PATH);
    }

    private void copyTemplateToTemp(Path relativePath) throws Exception {
        Path destination = tempDir.resolve(relativePath);
        Files.createDirectories(destination.getParent());
        Files.copy(TEMPLATE_ROOT.resolve(relativePath), destination, StandardCopyOption.REPLACE_EXISTING);
    }

    private void assertMatchesExpected(Path relativePath) throws Exception {
        String expected = Files.readString(EXPECTED_ROOT.resolve(relativePath));
        String actual = Files.readString(tempDir.resolve(relativePath));
        assertEquals(expected, actual, "Generated source mismatch for " + relativePath);
    }

    private Junit5ZephyrReport.Test junitTest(String className, String methodName) {
        return new Junit5ZephyrReport.Test(
            "id",
            methodName,
            className,
            methodName,
            Junit5ZephyrReport.Test.Status.PASSED,
            null,
            null,
            Set.of(),
            JiraAnnotationMetadata.empty()
        );
    }
}

package uk.hmcts.zephyr.automation.junit5;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Junit5TagServiceTest {

    private static final Path TEMPLATE_ROOT = Paths.get("src/test/resources/Junit5/template");
    private static final Path EXPECTED_ROOT = Paths.get("src/test/resources/Junit5/expected");
    private static final Path NESTED_RELATIVE_PATH = Path.of("uk/hmcts/zephyr/automation/util/SampleNestedTest.java");
    private static final Path IGNORE_RELATIVE_PATH = Path.of("uk/hmcts/zephyr/automation/util/SampleIgnoreTest.java");
    private static final Path PARAMETERIZED_RELATIVE_PATH =
        Path.of("uk/hmcts/zephyr/automation/util/SampleParameterizedJiraKeysTest.java");

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

    @Test
    void givenParameterizedTest_whenAddDifferentArgumentKeys_thenMatchesExpectedFixture() throws Exception {
        copyTemplateToTemp(PARAMETERIZED_RELATIVE_PATH);
        Junit5ZephyrReport.Test junitTest = junitTest(
            "uk.hmcts.zephyr.automation.util.SampleParameterizedJiraKeysTest",
            "parameterizedMethod"
        );
        junitTest.setType(Junit5ZephyrReport.Test.Type.PARAMETERIZED);

        junitTest.setArguments(List.of("false"));
        junitTest.setDisplayName("false");
        tagService.addTag(junitTest, new TestTag(TestTag.Type.JIRA_KEY, "ABC-101"));

        junitTest.setArguments(List.of("true"));
        junitTest.setDisplayName("true");
        tagService.addTag(junitTest, new TestTag(TestTag.Type.JIRA_KEY, "ABC-202"));

        assertMatchesExpected(PARAMETERIZED_RELATIVE_PATH);
    }

    @Test
    void givenParameterizedJiraKey_whenAddTag_thenFormatsDisplayNameAndStoresOriginalKey() {
        JavaTagger javaTagger = mock(JavaTagger.class);
        when(javaTagger.addAnnotation(anyString(), anyString(), any(), any())).thenReturn(true);
        final Junit5TagService parameterizedTagService = new Junit5TagService(javaTagger);
        final Junit5ZephyrReport.Test test =
            junitTest("uk.hmcts.zephyr.automation.util.SampleNestedTest$Nested1$Nested2", "targetMethod");
        test.setType(Junit5ZephyrReport.Test.Type.PARAMETERIZED);
        test.setDisplayName("alpha \"x\\y\"");
        TestTag tag = new TestTag(TestTag.Type.JIRA_KEY, "ABC-123");

        parameterizedTagService.addTag(test, tag);

        assertEquals("value = \"ABC-123\", name = \"alpha \\\"x\\\\y\\\"\"", tag.getValue());
        assertEquals(Set.of("ABC-123"), test.getMetadata().getJiraKey());
        verify(javaTagger).addAnnotation(anyString(), anyString(), any(), any());
    }

    @Test
    void givenStandardJiraKey_whenAddTag_thenFormatsAsQuotedString() {
        JavaTagger javaTagger = mock(JavaTagger.class);
        when(javaTagger.addAnnotation(anyString(), anyString(), any(), any())).thenReturn(true);
        Junit5TagService service = new Junit5TagService(javaTagger);
        Junit5ZephyrReport.Test test = junitTest("uk.hmcts.zephyr.automation.util.SampleIgnoreTest", "shouldSkip");
        TestTag tag = new TestTag(TestTag.Type.JIRA_KEY, "ABC-999");

        service.addTag(test, tag);

        assertEquals("\"ABC-999\"", tag.getValue());
        assertEquals(Set.of("ABC-999"), test.getMetadata().getJiraKey());
    }

    @Test
    void givenJiraIgnore_whenAddTag_thenSetsIgnoreMetadataWithoutLookupFailure() {
        JavaTagger javaTagger = mock(JavaTagger.class);
        when(javaTagger.addAnnotation(anyString(), anyString(), any(), any())).thenReturn(true);
        Junit5TagService service = new Junit5TagService(javaTagger);
        Junit5ZephyrReport.Test test = junitTest("uk.hmcts.zephyr.automation.util.SampleIgnoreTest", "shouldSkip");

        service.addTag(test, new TestTag(TestTag.Type.JIRA_IGNORE, null));

        assertTrue(test.getMetadata().isJiraIgnore());
    }

    @Nested
    class ExtractTagListFromTypeTest {

        @Test
        void givenJiraIgnoreFalse_whenExtractTagListFromType_thenReturnsFalseValueTag() {
            Junit5ZephyrReport.Test test = junitTest("uk.hmcts.zephyr.automation.util.SampleIgnoreTest", "shouldSkip");

            List<TestTag> result = tagService.extractTagListFromType(test, TestTag.Type.JIRA_IGNORE);

            assertEquals(1, result.size());
            assertEquals(TestTag.Type.JIRA_IGNORE, result.getFirst().getType());
            assertEquals("false", result.getFirst().getValue());
        }

        @Test
        void givenJiraComponents_whenExtractTagListFromType_thenReturnsAllComponentTags() {
            Junit5ZephyrReport.Test test = junitTest("uk.hmcts.zephyr.automation.util.SampleIgnoreTest", "shouldSkip");
            test.getMetadata().setJiraComponents(Set.of("api", "backend"));

            Set<String> resultValues = tagService.extractTagListFromType(test, TestTag.Type.JIRA_COMPONENT)
                .stream()
                .map(TestTag::getValue)
                .collect(Collectors.toSet());

            assertEquals(Set.of("api", "backend"), resultValues);
        }
    }

    @Nested
    class HasTagTest {

        @Test
        void givenJiraIgnoreTrue_whenHasTag_thenReturnsTrue() {
            Junit5ZephyrReport.Test test = junitTest("uk.hmcts.zephyr.automation.util.SampleIgnoreTest", "shouldSkip");
            test.getMetadata().setJiraIgnore(true);

            assertTrue(tagService.hasTag(test, TestTag.Type.JIRA_IGNORE));
        }

        @Test
        void givenJiraIgnoreFalse_whenHasTag_thenReturnsFalse() {
            Junit5ZephyrReport.Test test = junitTest("uk.hmcts.zephyr.automation.util.SampleIgnoreTest", "shouldSkip");

            assertFalse(tagService.hasTag(test, TestTag.Type.JIRA_IGNORE));
        }

        @Test
        void givenJiraStoryPresent_whenHasTag_thenReturnsTrueUsingDefaultPath() {
            Junit5ZephyrReport.Test test = junitTest("uk.hmcts.zephyr.automation.util.SampleIgnoreTest", "shouldSkip");
            test.getMetadata().setJiraStories(Set.of("story-1"));

            assertTrue(tagService.hasTag(test, TestTag.Type.JIRA_STORY));
        }
    }

    @Nested
    class AddTagTest {

        @Test
        void givenJiraComponent_whenAddTagAndAnnotationApplied_thenStoresOriginalMetadataValue() {
            JavaTagger javaTagger = mock(JavaTagger.class);
            when(javaTagger.addAnnotation(anyString(), anyString(), any(), any())).thenReturn(true);
            Junit5TagService service = new Junit5TagService(javaTagger);
            Junit5ZephyrReport.Test test = junitTest("uk.hmcts.zephyr.automation.util.SampleIgnoreTest", "shouldSkip");
            TestTag tag = new TestTag(TestTag.Type.JIRA_COMPONENT, "payments");

            service.addTag(test, tag);

            assertEquals("\"payments\"", tag.getValue());
            assertEquals(Set.of("payments"), test.getMetadata().getJiraComponents());
        }

        @Test
        void givenAnnotationNotApplied_whenAddTag_thenDoesNotMutateMetadata() {
            JavaTagger javaTagger = mock(JavaTagger.class);
            when(javaTagger.addAnnotation(anyString(), anyString(), any(), any())).thenReturn(false);
            Junit5TagService service = new Junit5TagService(javaTagger);
            Junit5ZephyrReport.Test test = junitTest("uk.hmcts.zephyr.automation.util.SampleIgnoreTest", "shouldSkip");

            service.addTag(test, new TestTag(TestTag.Type.JIRA_DEFECT, "BUG-1"));

            assertTrue(test.getMetadata().getJiraDefects().isEmpty());
        }

        @Test
        void givenNullTagType_whenAddTag_thenThrowsNullPointerException() {
            Junit5ZephyrReport.Test test = junitTest("uk.hmcts.zephyr.automation.util.SampleIgnoreTest", "shouldSkip");
            TestTag invalidTag = new TestTag(null, "ABC-1");

            NullPointerException exception =
                assertThrows(NullPointerException.class, () -> tagService.addTag(test, invalidTag));

            assertTrue(exception.getMessage().contains("pk") && exception.getMessage().contains("null"));
        }

        @Test
        void givenNullClassName_whenAddTag_thenThrowsNullPointerException() {
            Junit5ZephyrReport.Test test = junitTest(null, "shouldSkip");

            NullPointerException exception =
                assertThrows(NullPointerException.class,
                    () -> tagService.addTag(test, new TestTag(TestTag.Type.JIRA_KEY, "ABC-123")));

            assertEquals("className", exception.getMessage());
        }
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
            JiraAnnotationMetadata.empty(),
            List.of(),
            Junit5ZephyrReport.Test.Type.STANDARD,
            "id"
        );
    }
}

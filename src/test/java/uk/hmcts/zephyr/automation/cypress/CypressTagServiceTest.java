package uk.hmcts.zephyr.automation.cypress;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.TestTag;
import uk.hmcts.zephyr.automation.cypress.models.CypressReport;
import uk.hmcts.zephyr.automation.support.TestUtil;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class CypressTagServiceTest {

    private CypressTagService tagService;
    private CypressReport.CypressTest cypressTest;
    private MockedStatic<CypressTagger> cypressTaggerMock;

    @BeforeEach
    void setUp() throws Exception {
        TestUtil.resetSingletons();
        Config.instantiate(TestUtil.defaultArgs(
            Config.ActionType.CREATE_TICKETS, Config.ProcessType.CYPRESS_JSON_REPORT));
        tagService = new CypressTagService();
        cypressTest = new CypressReport.CypressTest();
        cypressTest.setTitle("User logs in");
        cypressTest.setParents(List.of("Login", "Successful path"));
        cypressTest.setFile("cypress/e2e/login.cy.js");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (cypressTaggerMock != null) {
            cypressTaggerMock.close();
            cypressTaggerMock = null;
        }
        TestUtil.resetSingletons();
    }

    @Nested
    class ExtractJiraKeyFromTagTest {

        @Test
        void givenTagContainingJiraKey_whenExtract_thenReturnsKey() {
            cypressTest.addTag(TagService.getTagPrefix(TestTag.Type.JIRA_KEY) + "ABC-123");

            Optional<String> jiraKey = tagService.extractJiraKeyFromTag(cypressTest);

            assertTrue(jiraKey.isPresent());
            assertEquals("ABC-123", jiraKey.get());
        }

        @Test
        void givenNoJiraTag_whenExtract_thenReturnsEmpty() {
            assertTrue(tagService.extractJiraKeyFromTag(cypressTest).isEmpty());
        }
    }

    @Nested
    class ExtractTagFromTagType {

        @Test
        void givenMatchingTag_whenExtract_thenReturnsSuffixWithoutPrefix() {
            cypressTest.addTag(TagService.getTagPrefix(TestTag.Type.JIRA_LABEL) + "critical");

            Optional<TestTag> label = tagService.extractTagFromTagType(cypressTest, TestTag.Type.JIRA_LABEL);

            assertTrue(label.isPresent());
            TestTag extractedTag = label.get();
            assertEquals(TestTag.Type.JIRA_LABEL, extractedTag.type());
            assertEquals("critical", extractedTag.value());
        }

        @Test
        void givenNoMatchingTag_whenExtract_thenReturnsEmpty() {
            assertTrue(tagService.extractTagFromTagType(cypressTest, TestTag.Type.JIRA_LABEL).isEmpty());
        }
    }

    @Nested
    class ExtractTagListFromTypeTest {

        @Test
        void givenMultipleMatchingTags_whenExtract_thenReturnsAllSuffixes() {
            cypressTest.addTag(TagService.getTagPrefix(TestTag.Type.JIRA_LABEL) + "critical");
            cypressTest.addTag(TagService.getTagPrefix(TestTag.Type.JIRA_LABEL) + "regression");
            cypressTest.addTag("@JIRA-OTHER:ignore");

            List<TestTag> labels = tagService.extractTagListFromType(cypressTest, TestTag.Type.JIRA_LABEL);

            assertEquals(2, labels.size());
            TestTag tag1 = labels.get(0);
            TestTag tag2 = labels.get(1);
            assertEquals(TestTag.Type.JIRA_LABEL, tag1.type());
            assertEquals(TestTag.Type.JIRA_LABEL, tag2.type());
            assertEquals("critical", tag1.value());
            assertEquals("regression", tag2.value());
        }
    }

    @Nested
    class AddTagTest {

        @Test
        void givenMissingTag_whenAddTag_thenDelegatesToTaggerAndAddsTag() {
            cypressTaggerMock = mockStatic(CypressTagger.class);
            TestTag testTag = new TestTag(TestTag.Type.JIRA_KEY, "ABC-456");
            tagService.addTag(cypressTest, testTag);

            String expectedTag = TagService.getTagPrefix(TestTag.Type.JIRA_KEY) + "ABC-456";
            cypressTaggerMock.verify(() -> CypressTagger.addTagToCypressTest(
                "/tmp/base/" + cypressTest.getFile(),
                cypressTest.getTitle(),
                expectedTag
            ));
            assertTrue(cypressTest.hasTag(expectedTag));
        }

        @Test
        void givenExistingTag_whenAddTag_thenSkipsTagger() {
            String expectedTag = TagService.getTagPrefix(TestTag.Type.JIRA_KEY) + "ABC-456";
            cypressTest.addTag(expectedTag);
            cypressTaggerMock = mockStatic(CypressTagger.class);
            TestTag testTag = new TestTag(TestTag.Type.JIRA_KEY, "ABC-456");
            tagService.addTag(cypressTest, testTag);

            cypressTaggerMock.verifyNoInteractions();
            assertEquals(1, cypressTest.getTags().size());
        }
    }
}

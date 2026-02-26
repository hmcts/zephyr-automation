package uk.hmcts.zephyr.automation.cypress;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cypress.models.CypressReport;
import uk.hmcts.zephyr.automation.support.TestUtil;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static uk.hmcts.zephyr.automation.jira.JiraConfig.JIRA_KEY_TAG_PREFIX;
import static uk.hmcts.zephyr.automation.jira.JiraConfig.JIRA_LABEL_TAG_PREFIX;

class CypressTagServiceTest {

    private CypressTagService tagService;
    private CypressReport.CypressTest cypressTest;
    private MockedStatic<CypressTagger> cypressTaggerMock;

    @BeforeEach
    void setUp() throws Exception {
        TestUtil.resetSingletons();
        Config.instantiate(TestUtil.defaultArgs(Config.ActionType.CREATE_TICKETS, Config.ProcessType.CYPRESS_JSON_REPORT));
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
            cypressTest.addTag("@" + JIRA_KEY_TAG_PREFIX + "ABC-123");

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
    class ExtractTagWithPrefixTest {

        @Test
        void givenMatchingTag_whenExtract_thenReturnsSuffixWithoutPrefix() {
            cypressTest.addTag("@" + JIRA_LABEL_TAG_PREFIX + "critical");

            Optional<String> label = tagService.extractTagWithPrefix(cypressTest, JIRA_LABEL_TAG_PREFIX);

            assertTrue(label.isPresent());
            assertEquals("critical", label.get());
        }

        @Test
        void givenNoMatchingTag_whenExtract_thenReturnsEmpty() {
            assertTrue(tagService.extractTagWithPrefix(cypressTest, JIRA_LABEL_TAG_PREFIX).isEmpty());
        }
    }

    @Nested
    class ExtractTagListWithPrefixTest {

        @Test
        void givenMultipleMatchingTags_whenExtract_thenReturnsAllSuffixes() {
            cypressTest.addTag("@" + JIRA_LABEL_TAG_PREFIX + "critical");
            cypressTest.addTag("@" + JIRA_LABEL_TAG_PREFIX + "regression");
            cypressTest.addTag("@JIRA-OTHER:ignore");

            List<String> labels = tagService.extractTagListWithPrefix(cypressTest, JIRA_LABEL_TAG_PREFIX);

            assertEquals(List.of("critical", "regression"), labels);
        }
    }

    @Nested
    class AddTagTest {

        @Test
        void givenMissingTag_whenAddTag_thenDelegatesToTaggerAndAddsTag() {
            cypressTaggerMock = mockStatic(CypressTagger.class);

            tagService.addTag(cypressTest, JIRA_KEY_TAG_PREFIX + "ABC-456");

            String expectedTag = "@" + JIRA_KEY_TAG_PREFIX + "ABC-456";
            cypressTaggerMock.verify(() -> CypressTagger.addTagToCypressTest(
                "/tmp/base/" + cypressTest.getFile(),
                cypressTest.getTitle(),
                expectedTag
            ));
            assertTrue(cypressTest.hasTag(expectedTag));
        }

        @Test
        void givenExistingTag_whenAddTag_thenSkipsTagger() {
            String expectedTag = "@" + JIRA_KEY_TAG_PREFIX + "ABC-456";
            cypressTest.addTag(expectedTag);
            cypressTaggerMock = mockStatic(CypressTagger.class);

            tagService.addTag(cypressTest, JIRA_KEY_TAG_PREFIX + "ABC-456");

            cypressTaggerMock.verifyNoInteractions();
            assertEquals(1, cypressTest.getTags().size());
        }
    }
}

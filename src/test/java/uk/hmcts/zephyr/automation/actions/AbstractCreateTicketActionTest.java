package uk.hmcts.zephyr.automation.actions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.TestTag;
import uk.hmcts.zephyr.automation.jira.JiraConfig;
import uk.hmcts.zephyr.automation.jira.client.Jira;
import uk.hmcts.zephyr.automation.jira.models.JiraIssue;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueFieldsWrapper;
import uk.hmcts.zephyr.automation.support.TestUtil;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractCreateTicketActionTest {

    private MockedStatic<Config> configMock;
    private TagService<ZephyrTest> tagService;
    private Jira jira;

    @BeforeEach
    void setUp() throws Exception {
        TestUtil.resetSingletons();
        JiraConfig.instantiate(TestUtil.defaultArgs(
            Config.ActionType.CREATE_TICKETS, Config.ProcessType.CUCUMBER_JSON_REPORT));
        configMock = mockStatic(Config.class);
        configMock.when(Config::getGithubRepoBaseSrcDir).thenReturn("/repo");
        configMock.when(Config::getBasePath).thenReturn("/base");
        configMock.when(Config::getReportPath).thenReturn("/report");
        @SuppressWarnings("unchecked")
        TagService<ZephyrTest> tagServiceMock = mock(TagService.class);
        tagService = tagServiceMock;
        jira = mock(Jira.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (configMock != null) {
            configMock.close();
        }
        TestUtil.resetSingletons();
    }

    @Test
    void givenTestWithExistingJiraKey_whenCreateJiraIssue_thenSkipsCreation() {
        TestCreateTicketAction action = new TestCreateTicketAction(tagService);
        ZephyrTest test = new DummyZephyrTest();

        when(tagService.extractJiraKeyFromTag(test)).thenReturn(Optional.of("ABC-1"));

        assertTrue(action.createJiraIssue(test).isEmpty());
        configMock.verify(() -> Config.getJira(), never());
    }

    @Test
    void givenTestMarkedIgnore_whenCreateJiraIssue_thenSkipsCreation() {
        TestCreateTicketAction action = new TestCreateTicketAction(tagService);
        ZephyrTest test = new DummyZephyrTest();

        when(tagService.extractJiraKeyFromTag(test)).thenReturn(Optional.empty());
        when(tagService.hasTag(test, TestTag.Type.JIRA_IGNORE)).thenReturn(true);

        assertTrue(action.createJiraIssue(test).isEmpty());
        configMock.verify(() -> Config.getJira(), never());
    }

    @Test
    void givenHappyPath_whenCreateJiraIssue_thenCreatesIssueAddsLinksAndTag() {
        TestCreateTicketAction action = new TestCreateTicketAction(tagService);
        ZephyrTest test = new DummyZephyrTest();
        JiraIssue createdIssue = JiraIssue.builder().key("CASE-9").build();

        when(tagService.extractJiraKeyFromTag(test)).thenReturn(Optional.empty());
        when(tagService.hasTag(test, TestTag.Type.JIRA_IGNORE)).thenReturn(false);
        configMock.when(Config::getJira).thenReturn(jira);
        when(jira.createIssue(action.bodyToReturn)).thenReturn(createdIssue);

        Optional<JiraIssue> result = action.createJiraIssue(test);

        assertTrue(result.isPresent());
        assertSame(createdIssue, result.get());
        assertSame(createdIssue.getKey(), action.lastLinkedIssueKey);
        assertSame(test, action.lastLinkedTest);
        verify(tagService).addTag(test, new TestTag(TestTag.Type.JIRA_KEY, createdIssue.getKey()));
    }

    @Test
    void givenCreateIssueThrows_whenCreateJiraIssue_thenReturnsEmpty() {
        final TestCreateTicketAction action = new TestCreateTicketAction(tagService);
        ZephyrTest test = new DummyZephyrTest();

        when(tagService.extractJiraKeyFromTag(test)).thenReturn(Optional.empty());
        when(tagService.hasTag(test, TestTag.Type.JIRA_IGNORE)).thenReturn(false);
        configMock.when(Config::getJira).thenReturn(jira);
        when(jira.createIssue(any())).thenThrow(new RuntimeException("failure"));

        assertTrue(action.createJiraIssue(test).isEmpty());
        verify(tagService, never()).addTag(any(), any());
    }

    private static class TestCreateTicketAction extends AbstractCreateTicketAction<ZephyrTest> {
        private final JiraIssueFieldsWrapper bodyToReturn = JiraIssueFieldsWrapper.builder().build();
        private String lastLinkedIssueKey;
        private ZephyrTest lastLinkedTest;

        TestCreateTicketAction(TagService<ZephyrTest> tagService) {
            super(tagService);
        }

        @Override
        protected JiraIssueFieldsWrapper buildBody(ZephyrTest test, boolean isCreate) {
            return bodyToReturn;
        }

        @Override
        protected void addLinksToJiraIssue(String sourceIssueKey, ZephyrTest test) {
            lastLinkedIssueKey = sourceIssueKey;
            lastLinkedTest = test;
        }

        @Override
        public void process() {
            // not needed for tests
        }
    }

    private static class DummyZephyrTest implements ZephyrTest {
        @Override
        public String getName() {
            return "scenario";
        }

        @Override
        public String getGitHubLink() {
            return "https://example";
        }

        @Override
        public String getNameAndLocation() {
            return "scenario (feature)";
        }

        @Override
        public ZephyrConstants.ExecutionStatus getZephyrExecutionStatus() {
            return ZephyrConstants.ExecutionStatus.PASS;
        }

        @Override
        public String getLocationDisplayName() {
            return "feature";
        }
    }
}

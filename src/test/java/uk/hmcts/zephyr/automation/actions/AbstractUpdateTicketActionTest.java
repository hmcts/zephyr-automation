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
import uk.hmcts.zephyr.automation.jira.models.JiraIssueFieldsWrapper;
import uk.hmcts.zephyr.automation.support.TestUtil;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractUpdateTicketActionTest {

    private MockedStatic<Config> configMock;
    private TagService<ZephyrTest> tagService;
    private Jira jira;

    @BeforeEach
    void setUp() throws Exception {
        TestUtil.resetSingletons();
        JiraConfig.instantiate(TestUtil.defaultArgs(
            Config.ActionType.UPDATE_TICKETS, Config.ProcessType.CUCUMBER_JSON_REPORT));
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
    void givenMissingJiraKey_whenUpdate_thenSkipsJiraCall() {
        TestUpdateTicketAction action = new TestUpdateTicketAction(tagService);
        ZephyrTest test = new DummyZephyrTest();

        when(tagService.extractJiraKeyFromTag(test)).thenReturn(Optional.empty());

        action.updateJiraIssue(test);

        configMock.verify(() -> Config.getJira(), never());
    }

    @Test
    void givenIgnoreTag_whenUpdate_thenSkipsJiraCall() {
        TestUpdateTicketAction action = new TestUpdateTicketAction(tagService);
        ZephyrTest test = new DummyZephyrTest();

        when(tagService.extractJiraKeyFromTag(test)).thenReturn(Optional.of("CASE-1"));
        when(tagService.hasTag(test, TestTag.Type.JIRA_IGNORE)).thenReturn(true);

        action.updateJiraIssue(test);

        configMock.verify(Config::getJira, never());
    }

    @Test
    void givenHappyPath_whenUpdate_thenUpdatesIssueAndAddsLinks() {
        final TestUpdateTicketAction action = new TestUpdateTicketAction(tagService);
        ZephyrTest test = new DummyZephyrTest();

        when(tagService.extractJiraKeyFromTag(test)).thenReturn(Optional.of("CASE-2"));
        when(tagService.hasTag(test, TestTag.Type.JIRA_IGNORE)).thenReturn(false);
        configMock.when(Config::getJira).thenReturn(jira);

        action.updateJiraIssue(test);

        verify(jira).updateIssue(action.bodyToReturn, "CASE-2");
        assertEquals("CASE-2", action.lastLinkedIssueKey);
        assertEquals(test, action.lastLinkedTest);
    }

    @Test
    void givenUpdateThrows_whenUpdate_thenSwallowsException() {
        final TestUpdateTicketAction action = new TestUpdateTicketAction(tagService);
        ZephyrTest test = new DummyZephyrTest();

        when(tagService.extractJiraKeyFromTag(test)).thenReturn(Optional.of("CASE-3"));
        when(tagService.hasTag(test, TestTag.Type.JIRA_IGNORE)).thenReturn(false);
        configMock.when(Config::getJira).thenReturn(jira);
        when(jira.updateIssue(any(), any())).thenThrow(new RuntimeException("boom"));

        assertDoesNotThrow(() -> action.updateJiraIssue(test));
    }

    private static class TestUpdateTicketAction extends AbstractUpdateTicketAction<ZephyrTest> {
        private final JiraIssueFieldsWrapper bodyToReturn = JiraIssueFieldsWrapper.builder().build();
        private String lastLinkedIssueKey;
        private ZephyrTest lastLinkedTest;

        TestUpdateTicketAction(TagService<ZephyrTest> tagService) {
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
            // not required for tests
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

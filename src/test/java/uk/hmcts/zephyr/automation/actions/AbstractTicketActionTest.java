package uk.hmcts.zephyr.automation.actions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.jira.JiraConfig;
import uk.hmcts.zephyr.automation.jira.client.Jira;
import uk.hmcts.zephyr.automation.jira.models.JiraComponent;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueFieldsWrapper;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueLink;
import uk.hmcts.zephyr.automation.support.TestUtil;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractTicketActionTest {

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
        jira = mock(Jira.class);
        configMock.when(Config::getJira).thenReturn(jira);
        @SuppressWarnings("unchecked")
        TagService<ZephyrTest> tagServiceMock = mock(TagService.class);
        tagService = tagServiceMock;
    }

    @AfterEach
    void tearDown() throws Exception {
        if (configMock != null) {
            configMock.close();
        }
        TestUtil.resetSingletons();
    }

    @Test
    void givenMissingGithubRepoPath_whenConstructing_thenThrows() {
        configMock.when(Config::getGithubRepoBaseSrcDir).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> new TestTicketAction(tagService));
    }

    @Test
    void givenMissingBasePath_whenConstructing_thenThrows() {
        configMock.when(Config::getBasePath).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> new TestTicketAction(tagService));
    }

    @Test
    void givenMissingReportPath_whenConstructing_thenThrows() {
        configMock.when(Config::getReportPath).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> new TestTicketAction(tagService));
    }

    @Test
    void givenTagsForLinking_whenAddLinksToJiraIssue_thenCreatesExpectedLinks() {
        final TestTicketAction action = new TestTicketAction(tagService);
        ZephyrTest test = new DummyZephyrTest();
        when(tagService.extractTagListWithPrefix(test, JiraConfig.JIRA_NFR_TAG_PREFIX))
            .thenReturn(List.of("NFR-1"));
        when(tagService.extractTagListWithPrefix(test, JiraConfig.JIRA_LINK_TAG_PREFIX))
            .thenReturn(List.of("LINK-1"));
        when(tagService.extractTagListWithPrefix(test, JiraConfig.JIRA_STORY_TAG_PREFIX))
            .thenReturn(List.of("STORY-1"));
        when(tagService.extractTagListWithPrefix(test, JiraConfig.JIRA_DEFECT_TAG_PREFIX))
            .thenReturn(List.of("BUG-1"));

        action.addLinksToJiraIssue("CASE-1", test);

        ArgumentCaptor<JiraIssueLink> captor = ArgumentCaptor.forClass(JiraIssueLink.class);
        verify(jira, times(4)).linkIssue(captor.capture());
        List<JiraIssueLink> links = captor.getAllValues();
        assertEquals("CASE-1", links.getFirst().getOutwardIssue().getKey());
        assertEquals("NFR-1", links.getFirst().getInwardIssue().getKey());
        assertEquals("Contributes", links.getFirst().getType().getName());
        assertEquals("LINK-1", links.get(1).getInwardIssue().getKey());
        assertEquals("Relates", links.get(1).getType().getName());
        assertEquals("STORY-1", links.get(2).getInwardIssue().getKey());
        assertEquals("BUG-1", links.get(3).getInwardIssue().getKey());
    }

    @Test
    void givenTest_whenGetJiraDescription_thenFormatsLocationAndScenario() {
        TestTicketAction action = new TestTicketAction(tagService);
        ZephyrTest test = new DummyZephyrTest();

        String description = action.getJiraDescription(test);

        assertEquals("Location: [feature|https://example]\r\nScenario: scenario\r\n", description);
    }

    @Test
    void givenLabels_whenGetLabels_thenDelegatesToTagService() {
        TestTicketAction action = new TestTicketAction(tagService);
        ZephyrTest test = new DummyZephyrTest();
        when(tagService.extractTagListWithPrefix(test, JiraConfig.JIRA_LABEL_TAG_PREFIX))
            .thenReturn(List.of("critical", "smoke"));

        List<String> labels = action.getLabels(test);

        assertEquals(List.of("critical", "smoke"), labels);
    }

    @Test
    void givenDefaultAndTaggedComponents_whenGetComponents_thenResolvesIds() {
        TestTicketAction action = new TestTicketAction(tagService);
        ZephyrTest test = new DummyZephyrTest();
        when(tagService.extractTagListWithPrefix(test, JiraConfig.JIRA_COMPONENT_TAG_PREFIX))
            .thenReturn(List.of("Extra"));
        when(jira.getComponentByName(JiraConfig.getProjectId(), "Default"))
            .thenReturn(componentWithId("id-default"));
        when(jira.getComponentByName(JiraConfig.getProjectId(), "Extra"))
            .thenReturn(componentWithId("id-extra"));

        List<JiraIssueFieldsWrapper.Component> components = action.getComponents(test);

        assertEquals(List.of("id-default", "id-extra"),
            components.stream().map(JiraIssueFieldsWrapper.Component::getId).toList());
    }

    @Test
    void givenEpicTag_whenGetEpicTicketKey_thenReturnsOptional() {
        TestTicketAction action = new TestTicketAction(tagService);
        ZephyrTest test = new DummyZephyrTest();
        when(tagService.extractTagWithPrefix(test, JiraConfig.JIRA_EPIC_TAG_PREFIX)).thenReturn(Optional.of("EPIC-1"));

        assertEquals(Optional.of("EPIC-1"), action.getEpicTicketKey(test));
    }

    @Test
    void givenTest_whenBuildBodyForCreate_thenPopulatesAllFields() {
        TestTicketAction action = new TestTicketAction(tagService);
        ZephyrTest test = new DummyZephyrTest();
        when(tagService.extractTagListWithPrefix(test, JiraConfig.JIRA_LABEL_TAG_PREFIX))
            .thenReturn(List.of("critical"));
        when(tagService.extractTagListWithPrefix(test, JiraConfig.JIRA_COMPONENT_TAG_PREFIX))
            .thenReturn(List.of("Extra"));
        when(tagService.extractTagWithPrefix(test, JiraConfig.JIRA_EPIC_TAG_PREFIX))
            .thenReturn(Optional.of("EPIC-9"));
        when(jira.getComponentByName(JiraConfig.getProjectId(), "Default"))
            .thenReturn(componentWithId("id-default"));
        when(jira.getComponentByName(JiraConfig.getProjectId(), "Extra"))
            .thenReturn(componentWithId("id-extra"));

        JiraIssueFieldsWrapper createBody = action.buildBody(test, true);

        JiraIssueFieldsWrapper.Fields fields = createBody.getFields();
        assertEquals("scenario", fields.getSummary());
        assertEquals("Location: [feature|https://example]\r\nScenario: scenario\r\n", fields.getDescription());
        assertEquals(JiraConfig.getProjectId(), fields.getProject().getId());
        assertEquals(ZephyrConstants.ZEPHYR_ISSUE_TYPE_ID, fields.getIssuetype().getId());
        assertEquals(JiraConfig.getDefaultUser(), fields.getReporter().getName());
        assertEquals(List.of("critical"), fields.getLabels());
        assertEquals(List.of("id-default", "id-extra"),
            fields.getComponents().stream().map(JiraIssueFieldsWrapper.Component::getId).toList());
        assertEquals("EPIC-9", fields.getDynamicFields().get(JiraConfig.getEpicLinkCustomFieldId()));

        JiraIssueFieldsWrapper updateBody = action.buildBody(test, false);
        assertEquals("scenario", updateBody.getFields().getSummary());
        assertEquals("Location: [feature|https://example]\r\nScenario: scenario\r\n", updateBody.getFields().getDescription());
        assertNull(updateBody.getFields().getProject());
        assertNull(updateBody.getFields().getIssuetype());
        assertNull(updateBody.getFields().getReporter());
    }

    private JiraComponent componentWithId(String id) {
        JiraComponent component = new JiraComponent();
        component.setId(id);
        return component;
    }

    private static class TestTicketAction extends AbstractTicketAction<ZephyrTest> {
        TestTicketAction(TagService<ZephyrTest> tagService) {
            super(tagService);
        }

        @Override
        public void process() {
            // no-op for tests
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

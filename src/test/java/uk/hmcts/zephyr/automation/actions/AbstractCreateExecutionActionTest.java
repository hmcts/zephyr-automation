package uk.hmcts.zephyr.automation.actions;

import feign.form.FormData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.jira.JiraConfig;
import uk.hmcts.zephyr.automation.jira.client.Jira;
import uk.hmcts.zephyr.automation.jira.models.JiraSearchResponse;
import uk.hmcts.zephyr.automation.support.TestUtil;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;
import uk.hmcts.zephyr.automation.zephyr.client.Zephyr;
import uk.hmcts.zephyr.automation.zephyr.models.JobProgressToken;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrBulkExecutionRequest;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrBulkExecutionResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycle;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycleResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionSearchResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionStatusUpdateRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractCreateExecutionActionTest {

    private MockedStatic<Config> configMock;
    private TagService<ZephyrTest> tagService;

    @BeforeEach
    void setUp() throws Exception {
        TestUtil.resetSingletons();
        String[] args = TestUtil.defaultArgs(
            Config.ActionType.CREATE_EXECUTION, Config.ProcessType.CYPRESS_JSON_REPORT);
        JiraConfig.instantiate(args);
        configMock = mockStatic(Config.class);
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
    void givenMissingReportPath_whenConstructed_thenThrows() {
        configMock.when(Config::getReportPath).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> new TestCreateExecutionAction(tagService));
    }

    @Test
    void givenTestsWithoutJiraKeys_whenProcessTests_thenSkipsZephyrInteractions() {
        configMock.when(Config::getReportPath).thenReturn("/tmp/report.json");
        TestCreateExecutionAction action = new TestCreateExecutionAction(tagService);
        ZephyrTest test = new DummyZephyrTest("Scenario without key", ZephyrConstants.ExecutionStatus.PASS);
        when(tagService.extractJiraKeyFromTag(test)).thenReturn(Optional.empty());

        action.processTests(List.of(test));

        configMock.verify(() -> Config.getJira(), never());
        configMock.verify(() -> Config.getZephyr(), never());
    }

    @Test
    void givenTestsWithJiraKeys_whenProcessTests_thenCreatesCycleAndUpdatesExecutions() {
        Zephyr zephyr = mock(Zephyr.class);
        Jira jira = mock(Jira.class);
        configMock.when(Config::getReportPath).thenReturn("/tmp/report.json");
        configMock.when(Config::getZephyr).thenReturn(zephyr);
        configMock.when(Config::getJira).thenReturn(jira);
        configMock.when(Config::getExecutionBuild).thenReturn("Some-Build");
        configMock.when(Config::getExecutionEnvironment).thenReturn("Some-Env");
        final TestCreateExecutionAction action = new TestCreateExecutionAction(tagService);

        ZephyrTest test = new DummyZephyrTest("Scenario with key", ZephyrConstants.ExecutionStatus.PASS);
        when(tagService.extractJiraKeyFromTag(test)).thenReturn(Optional.of("CASE-1"));

        JiraSearchResponse.JiraIssueSummary issueSummary = JiraSearchResponse.JiraIssueSummary.builder()
            .key("CASE-1")
            .id("101")
            .build();
        JiraSearchResponse searchResponse = JiraSearchResponse.builder()
            .issues(List.of(issueSummary))
            .build();
        when(jira.searchIssues(any())).thenReturn(searchResponse);

        ZephyrCycleResponse cycleResponse = new ZephyrCycleResponse();
        cycleResponse.setId("cycle-1");
        when(zephyr.createCycle(any())).thenReturn(cycleResponse);
        when(zephyr.addTestsToCycle(any())).thenReturn(JobProgressToken.builder().jobProgressToken("job-123").build());
        when(zephyr.getAddTestsToCycleJobProgress("job-123"))
            .thenReturn(ZephyrBulkExecutionResponse.builder().progress(1.0).build());

        ZephyrExecutionSearchResponse.Execution execution = new ZephyrExecutionSearchResponse.Execution();
        execution.setIssueKey("CASE-1");
        execution.setId(55L);
        ZephyrExecutionSearchResponse executionSearchResponse = new ZephyrExecutionSearchResponse();
        executionSearchResponse.setExecutions(List.of(execution));
        when(zephyr.searchExecutions("cycle-1")).thenReturn(executionSearchResponse);

        action.processTests(List.of(test));

        ArgumentCaptor<ZephyrCycle> zephyrCycleCaptor = ArgumentCaptor.forClass(ZephyrCycle.class);
        verify(zephyr).createCycle(zephyrCycleCaptor.capture());

        ZephyrCycle cycle = zephyrCycleCaptor.getValue();
        assertEquals("Automated Cycle -", cycle.getName().substring(0, 17));
        assertEquals("Cycle created automatically from Cucumber JSON report", cycle.getDescription());
        assertEquals(JiraConfig.getProjectId(), cycle.getProjectId());
        assertEquals("Some-Env", cycle.getEnvironment());
        assertEquals("Some-Build", cycle.getBuild());


        verify(jira).searchIssues(any());
        ArgumentCaptor<ZephyrBulkExecutionRequest> bulkExecutionCaptor =
            ArgumentCaptor.forClass(ZephyrBulkExecutionRequest.class);
        verify(zephyr).addTestsToCycle(bulkExecutionCaptor.capture());
        assertEquals(List.of("CASE-1"), bulkExecutionCaptor.getValue().getIssues());

        ArgumentCaptor<ZephyrExecutionStatusUpdateRequest> statusCaptor =
            ArgumentCaptor.forClass(ZephyrExecutionStatusUpdateRequest.class);
        verify(zephyr).updateExecutionStatus(statusCaptor.capture());
        assertEquals(List.of(String.valueOf(execution.getId())), statusCaptor.getValue().getExecutions());
        assertEquals(String.valueOf(ZephyrConstants.ExecutionStatus.PASS.getStatusId()),
            statusCaptor.getValue().getStatus());
    }

    @Test
    void attachFileToExecution_givenAttachment_whenCalled_thenInvokesZephyrClientWithFormData() {
        Zephyr zephyr = mock(Zephyr.class);
        configMock.when(Config::getZephyr).thenReturn(zephyr);
        configMock.when(Config::getReportPath).thenReturn("/tmp/report.json");
        configMock.when(Config::getZephyr).thenReturn(zephyr);
        configMock.when(Config::getExecutionBuild).thenReturn("Some-Build");
        configMock.when(Config::getExecutionEnvironment).thenReturn("Some-Env");
        final TestCreateExecutionAction action = new TestCreateExecutionAction(tagService);

        Attachment attachment = new TestAttachment("screenshot.png", "image/png", new byte[]{1, 2, 3});
        Long executionId = 123L;

        action.attachFileToExecution(executionId, attachment);

        ArgumentCaptor<String> entityTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> entityIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<FormData> formDataCaptor = ArgumentCaptor.forClass(FormData.class);
        verify(zephyr).attachEvidence(entityTypeCaptor.capture(), entityIdCaptor.capture(), formDataCaptor.capture());

        assertEquals("EXECUTION", entityTypeCaptor.getValue());
        assertEquals(executionId, entityIdCaptor.getValue());
        FormData formData = formDataCaptor.getValue();
        assertEquals(attachment.getContentType(), formData.getContentType());
        assertEquals(attachment.getFileName(), formData.getFileName());
        assertEquals(attachment.getContent(), formData.getData());
    }

    record TestAttachment(String fileName, String contentType, byte[] content) implements Attachment {
        @Override
        public String getFileName() {
            return TestAttachment.this.fileName;
        }

        @Override
        public String getContentType() {
            return TestAttachment.this.contentType;
        }

        @Override
        public byte[] getContent() {
            return TestAttachment.this.content;
        }
    }

    private static class TestCreateExecutionAction extends AbstractCreateExecutionAction<ZephyrTest> {
        TestCreateExecutionAction(TagService<ZephyrTest> tagService) {
            super(tagService);
        }

        @Override
        public void process() {
            // No-op for testing hooks around processTests
        }
    }

    private static class DummyZephyrTest implements ZephyrTest {
        private final String name;
        private final ZephyrConstants.ExecutionStatus status;

        DummyZephyrTest(String name, ZephyrConstants.ExecutionStatus status) {
            this.name = name;
            this.status = status;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getGitHubLink() {
            return "https://example";
        }

        @Override
        public String getNameAndLocation() {
            return name;
        }

        @Override
        public ZephyrConstants.ExecutionStatus getZephyrExecutionStatus() {
            return status;
        }

        @Override
        public String getLocationDisplayName() {
            return "feature";
        }
    }
}

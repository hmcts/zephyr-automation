package uk.hmcts.zephyr.automation.actions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.jira.JiraConfig;
import uk.hmcts.zephyr.automation.jira.models.JiraSearchRequest;
import uk.hmcts.zephyr.automation.jira.models.JiraSearchResponse;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycle;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycleResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionDetail;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionRequest;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionStatusUpdateRequest;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Getter
@Slf4j
public abstract class AbstractCreateExecutionAction<T extends ZephyrTest>
    extends AbstractAction<T>
    implements CreateExecutionAction {

    protected AbstractCreateExecutionAction(TagService<T> tagService) {
        super(tagService);
        validateConfig();
    }

    private void validateConfig() {
        if (Config.getReportPath() == null) {
            throw new IllegalArgumentException(
                "For CREATE_EXECUTION action type, report-path must be specified as a command line "
                    + "argument");
        }
    }

    protected void processTests(List<T> tests) {

        List<ScenarioResult> scenarioResults = new ArrayList<>();
        for (T test : tests) {
            getScenarioResultFromTest(test).ifPresent(scenarioResults::add);
        }
        assignJiraIds(scenarioResults);
        ZephyrCycleResponse cycle = Config.getZephyr().createCycle(
            ZephyrCycle.builder()
                .name("Automated Cycle - " + DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now()))
                .description("Cycle created automatically from Cucumber JSON report")
                .projectId(JiraConfig.getProjectId())
                .versionId("-1") //Default to -1 for no version
                .build()
        );
        assignExecutionDetails(scenarioResults, cycle.getId());
        updateExecutionStatus(scenarioResults);
    }


    private void updateExecutionStatus(List<ScenarioResult> scenarioResults) {
        Map<ZephyrConstants.ExecutionStatus, List<ScenarioResult>> executionsByStatus = scenarioResults.stream()
            .filter(sr -> sr.getExecutionDetail() != null) //Filter out scenarios where execution creation failed
            .collect(Collectors.groupingBy(ScenarioResult::getStatus));

        //Bulk update execution status for each status group to minimize API calls

        executionsByStatus.forEach((key, value) -> {
            ZephyrExecutionStatusUpdateRequest request = ZephyrExecutionStatusUpdateRequest.builder()
                .executions(value.stream()
                    .map(sr -> sr.getExecutionDetail().getId())
                    .map(String::valueOf)
                    .toList())
                .status(String.valueOf(key.getStatusId()))
                .build();
            Config.getZephyr().updateExecutionStatus(request);
        });
    }

    private void assignExecutionDetails(List<ScenarioResult> scenarioResults, String cycleId) {
        for (ScenarioResult scenarioResult : scenarioResults) {
            if (scenarioResult.getIssueId() == null) {
                log.warn("Skipping execution creation for scenario with issue key: {} as Jira issue ID is missing",
                    scenarioResult.getIssueKey());
                continue;
            }
            ZephyrExecutionRequest executionRequest = ZephyrExecutionRequest.builder()
                .cycleId(cycleId)
                .issueId(scenarioResult.getIssueId())
                .projectId(JiraConfig.getProjectId())
                .versionId("-1") //Default to -1 for no version
                .assigneeType("assignee")
                .assignee(JiraConfig.getDefaultUser())
                .build();
            Map<String, ZephyrExecutionDetail> executionDetailMap =
                Config.getZephyr().createExecution(executionRequest);
            ZephyrExecutionDetail executionDetail = executionDetailMap.entrySet().stream().findFirst().get().getValue();
            scenarioResult.setExecutionDetail(executionDetail);
        }
    }

    private void assignJiraIds(List<ScenarioResult> scenarioResults) {
        //Bulk get all issueIds from issueKeys to minimize API calls
        List<String> issueKeys = scenarioResults.stream()
            .map(ScenarioResult::getIssueKey)
            .toList();

        JiraSearchResponse searchResponse = Config.getJira().searchIssues(
            JiraSearchRequest.builder()
                .jql("key in (" + String.join(",", issueKeys) + ")")
                .fields(List.of("id"))
                .maxResults(scenarioResults.size())
                .build());

        Map<String, String> issueKeyToIdMap = searchResponse.getIssues()
            .stream()
            .collect(Collectors.toMap(JiraSearchResponse.JiraIssueSummary::getKey,
                JiraSearchResponse.JiraIssueSummary::getId));

        for (ScenarioResult scenarioResult : scenarioResults) {
            String issueId = issueKeyToIdMap.get(scenarioResult.getIssueKey());
            if (issueId == null) {
                log.warn("Could not find Jira issue ID for key: {}", scenarioResult.getIssueKey());
                continue;
            }
            scenarioResult.setIssueId(issueId);
        }
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    static class ScenarioResult {
        private final String issueKey;
        private final ZephyrConstants.ExecutionStatus status;
        private String issueId; //To be populated after fetching from Jira
        private ZephyrExecutionDetail executionDetail;//To be populated after creating execution in Zephyr

    }

    private Optional<ScenarioResult> getScenarioResultFromTest(T test) {
        Optional<String> issueKeyOpt = getTagService().extractJiraKeyFromTag(test);
        if (issueKeyOpt.isEmpty()) {
            log.warn("No Jira issue key found for test: {}", test.getNameAndLocation());
            return Optional.empty();
        }
        String issueKey = issueKeyOpt.get();
        ZephyrConstants.ExecutionStatus status = test.getZephyrExecutionStatus();
        return Optional.of(new ScenarioResult(issueKey, status));
    }
}

package uk.hmcts.zephyr.automation.actions;

import feign.form.FormData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.jira.JiraConfig;
import uk.hmcts.zephyr.automation.jira.models.JiraSearchRequest;
import uk.hmcts.zephyr.automation.jira.models.JiraSearchResponse;
import uk.hmcts.zephyr.automation.util.Util;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;
import uk.hmcts.zephyr.automation.zephyr.models.JobProgressToken;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrBulkExecutionRequest;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrBulkExecutionResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycle;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycleResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionSearchResponse;
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

    public List<ScenarioResult> processTests(List<T> tests) {

        List<ScenarioResult> scenarioResults = new ArrayList<>();
        for (T test : tests) {
            getScenarioResultFromTest(test).ifPresent(scenarioResults::add);
        }
        if (scenarioResults.isEmpty()) {
            log.info("No scenario results found");
            return List.of();
        }
        assignJiraIds(scenarioResults);
        ZephyrCycleResponse cycle = Config.getZephyr().createCycle(
            ZephyrCycle.builder()
                .name(
                    Optional.ofNullable(Config.getTestCycleName())
                        .orElse("Automated Cycle - " + DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())))
                .description(
                    Optional.ofNullable(Config.getTestCycleDescription())
                        .orElse("Cycle created automatically from Cucumber JSON report"))
                .projectId(JiraConfig.getProjectId())
                .environment(Config.getExecutionEnvironment())
                .build(Config.getExecutionBuild())
                .versionId(
                    Optional.ofNullable(Config.getTestCycleVersion())
                        .orElse("-1"))//Default to -1 for no version
                .build()
        );
        assignExecutionDetails(scenarioResults, cycle.getId());
        updateExecutionStatus(scenarioResults);
        return scenarioResults;
    }

    public void attachFileToExecution(Long executionId, Attachment attachment) {
        FormData formData = new FormData(
            attachment.getContentType(),
            attachment.getFileName(),
            attachment.getContent()
        );
        Config.getZephyr().attachEvidence("EXECUTION", executionId, formData);
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

        List<String> jiraKeys = scenarioResults.stream()
            .map(ScenarioResult::getIssueKey)
            .toList();

        //Bulk link test to the cycle to minimize API
        ZephyrBulkExecutionRequest bulkExecutionRequest = ZephyrBulkExecutionRequest.builder()
            .cycleId(cycleId)
            .issues(jiraKeys)
            .method("1")
            .projectId(JiraConfig.getProjectId())
            .build();

        JobProgressToken jobProgressToken = Config.getZephyr().addTestsToCycle(bulkExecutionRequest);

        //Poll Zephyr for job progress and wait until executions are created before proceeding to update execution
        // details
        ZephyrBulkExecutionResponse jobProgressResponse;
        long startTime = System.currentTimeMillis();
        boolean firstPoll = true;
        log.info("Polling Zephyr for job progress of adding tests to cycle. Job progress token: {}", jobProgressToken);
        do {
            jobProgressResponse =
                Config.getZephyr().getAddTestsToCycleJobProgress(jobProgressToken.getJobProgressToken());

            if (firstPoll) {
                firstPoll = false;
            } else {
                log.info("Waiting for Zephyr job to complete. Job progress response: {}",
                    Util.writeObjectToString(jobProgressResponse));
                try {
                    //Wait before polling to avoid hitting rate limits and give Zephyr some time to process the request
                    Thread.sleep(Config.DEFAULT_WAIT_TIME);
                } catch (InterruptedException e) {
                    log.error("Thread interrupted while waiting for Zephyr job to complete", e);
                    Thread.currentThread().interrupt();
                    return;
                }
            }

        } while (!jobProgressResponse.isCompleted() && !jobProgressResponse.isFailed()
            && (System.currentTimeMillis() - startTime) < Config.DEFAULT_TIMEOUT);

        if (!jobProgressResponse.isCompleted()) {
            log.error("Zephyr job did not complete within the expected time. Job progress response: {}",
                jobProgressResponse);
            throw new RuntimeException("Zephyr job did not complete within the expected time.");
        }

        log.info("Mapping Zephyr executions to scenario results. Job progress response: {}", jobProgressResponse);
        //Fetch all executions for the cycle and map them by issue key to assign execution details to scenario results
        ZephyrExecutionSearchResponse executionSearchResponse = Config.getZephyr().searchExecutions(cycleId);

        Map<String, ZephyrExecutionSearchResponse.Execution> issueKeyToExecutionMap =
            executionSearchResponse.getExecutions()
                .stream()
                .collect(Collectors.toMap(ZephyrExecutionSearchResponse.Execution::getIssueKey, e -> e));

        for (ScenarioResult scenarioResult : scenarioResults) {
            ZephyrExecutionSearchResponse.Execution execution =
                issueKeyToExecutionMap.get(scenarioResult.getIssueKey());
            if (execution == null) {
                log.warn("Could not find execution in Zephyr for issue key: {}", scenarioResult.getIssueKey());
                continue;
            }
            scenarioResult.setExecutionDetail(execution);
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
    public class ScenarioResult {
        private final T test;
        private final String issueKey;
        private final ZephyrConstants.ExecutionStatus status;
        //The below will be populated after creating execution in Zephyr
        private String issueId;
        private ZephyrExecutionSearchResponse.Execution executionDetail;

    }

    private Optional<ScenarioResult> getScenarioResultFromTest(T test) {
        Optional<String> issueKeyOpt = getTagService().extractJiraKeyFromTag(test);
        if (issueKeyOpt.isEmpty()) {
            log.warn("No Jira issue key found for test: {}", test.getNameAndLocation());
            return Optional.empty();
        }
        String issueKey = issueKeyOpt.get();
        ZephyrConstants.ExecutionStatus status = test.getZephyrExecutionStatus();
        return Optional.of(new ScenarioResult(test, issueKey, status));
    }
}

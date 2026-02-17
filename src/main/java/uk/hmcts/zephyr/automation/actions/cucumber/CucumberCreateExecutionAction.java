package uk.hmcts.zephyr.automation.actions.cucumber;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.actions.CreateExecutionAction;
import uk.hmcts.zephyr.automation.cucumber.report.Element;
import uk.hmcts.zephyr.automation.cucumber.report.Feature;
import uk.hmcts.zephyr.automation.jira.JiraConstants;
import uk.hmcts.zephyr.automation.jira.models.JiraSearchRequest;
import uk.hmcts.zephyr.automation.jira.models.JiraSearchResponse;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycle;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrCycleResponse;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionDetail;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionRequest;
import uk.hmcts.zephyr.automation.zephyr.models.ZephyrExecutionStatusUpdateRequest;
import uk.hmcts.zephyr.util.TagUtil;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.hmcts.zephyr.automation.Config.JIRA;
import static uk.hmcts.zephyr.automation.Config.ZEPHYR;

@Slf4j
public class CucumberCreateExecutionAction extends AbstractCucumberAction implements CreateExecutionAction {

    public CucumberCreateExecutionAction(String[] args) {
        super(args);

    }

    @Override
    public void process() {
        log.info("Starting Cucumber Create Execution Action");
        List<Feature> features = getFeatures();
        if (features == null || features.isEmpty()) {
            log.warn("No features found to process for execution creation");
            return;
        }
        List<ScenarioResult> scenarioResults = new ArrayList<>();
        for (Feature feature : features) {
            scenarioResults.addAll(getScenarioResultFromFeature(feature));
        }
        assignJiraIds(scenarioResults);
        ZephyrCycleResponse cycle = ZEPHYR.createCycle(
            ZephyrCycle.builder()
                .name("Automated Cycle - " + DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now()))
                .description("Cycle created automatically from Cucumber JSON report")
                .projectId(JiraConstants.PROJECT_ID)
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
            ZEPHYR.updateExecutionStatus(request);
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
                .projectId(JiraConstants.PROJECT_ID)
                .versionId("-1") //Default to -1 for no version
                .assigneeType("assignee")
                .assignee(JiraConstants.DEFAULT_USER)
                .build();
            Map<String, ZephyrExecutionDetail> executionDetailMap = ZEPHYR.createExecution(executionRequest);
            ZephyrExecutionDetail executionDetail = executionDetailMap.entrySet().stream().findFirst().get().getValue();
            scenarioResult.setExecutionDetail(executionDetail);
        }
    }

    private void assignJiraIds(List<ScenarioResult> scenarioResults) {
        //Bulk get all issueIds from issueKeys to minimize API calls
        List<String> issueKeys = scenarioResults.stream()
            .map(ScenarioResult::getIssueKey)
            .toList();

        JiraSearchResponse searchResponse = JIRA.searchIssues(
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

    private List<ScenarioResult> getScenarioResultFromFeature(Feature feature) {
        List<ScenarioResult> scenarioResults = new ArrayList<>();
        for (Element element : feature.getElements()) {
            getScenarioResultFromScenario(feature, element)
                .ifPresent(scenarioResults::add);
        }
        return scenarioResults;
    }

    private Optional<ScenarioResult> getScenarioResultFromScenario(Feature feature, Element element) {
        Optional<String> issueKeyOpt = TagUtil.extractJiraKeyFromTag(element);
        if (issueKeyOpt.isEmpty()) {
            log.warn("No Jira issue key found for scenario: {} in feature: {}", element.getName(), feature.getName());
            return Optional.empty();
        }
        String issueKey = issueKeyOpt.get();
        ZephyrConstants.ExecutionStatus status = determineExecutionStatus(element);
        return Optional.of(new ScenarioResult(issueKey, status));


    }

    private ZephyrConstants.ExecutionStatus determineExecutionStatus(Element element) {
        //If all steps passed, mark as pass.
        if (element.getSteps().stream()
            .map(step -> step.getResult().getStatus())
            .allMatch(s -> s.equalsIgnoreCase("passed"))) {
            return ZephyrConstants.ExecutionStatus.PASS;
        }
        //If any step failed, mark as fail.
        if (element.getSteps().stream()
            .map(step -> step.getResult().getStatus())
            .anyMatch(s -> s.equalsIgnoreCase("failed"))) {
            return ZephyrConstants.ExecutionStatus.FAIL;
        }
        //Default to unexecuted if there are no steps or all steps are skipped or undefined
        return ZephyrConstants.ExecutionStatus.UNEXECUTED;
    }
}

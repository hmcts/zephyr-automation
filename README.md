# Zephyr Automation

Zephyr Automation is a tool for integrating test results and ticket management between Zephyr, Jira, and test frameworks (Cucumber, Cypress). It automates the creation and updating of Jira tickets and Zephyr executions based on test reports.

## Features
- Create or update Jira tickets from test results
- Create Zephyr executions
- Supports Cucumber and Cypress JSON reports

## Usage

### Command-line Arguments

**Required Arguments:**
- `action-type=`: The action to perform. Allowed values:
  - `CREATE_TICKETS`: Create new Jira tickets from test results
  - `UPDATE_TICKETS`: Update existing Jira tickets from test results
  - `CREATE_EXECUTION`: Create a Zephyr execution from test results
- `process-type=`: The type of test report to process. Allowed values:
  - `CUCUMBER_JSON_REPORT`: Process Cucumber JSON reports (cucumber.json)
  - `CYPRESS_JSON_REPORT`: Process Cypress JSON reports (Requires the ZephyrReporter.ts cypress reporter to be used in the cypress tests)
- `jira-base-url=`: Base URL for the Jira instance (e.g., `https://your-domain.atlassian.net`)
- `jira-project-id=`: Jira project ID
- `jira-default-user=`: Default Jira user for ticket actions
- `jira-auth-token=`: Jira authentication token
- `base-path=`: Base path for input files (e.g., test reports)
- `report-path=`: Path to output the generated report

**Required for Action Type: CREATE_TICKETS or UPDATE_TICKETS:**
- `github-repo-base-src-dir=`: Base source directory for the GitHub repository
- `jira-epic-link-custom-field-id=`: Custom field ID for Jira epic links
- `jira-default-components=`: Comma-separated list of default Jira components (e.g., `ComponentA,ComponentB`)

**Optional Arguments:*
- `execution-environment=`: Environment for the Zephyr execution (e.g., `Staging`, `Production`)
- `execution-build=`:  Build version for the Zephyr execution (e.g., `1.0.0`)
- `execution-test-cycle-name=`: Test cycle name for the Zephyr execution (e.g., `Regression Cycle 1`)
- `execution-test-cycle-description=`: Test cycle description for the Zephyr execution (e.g., `Automated test execution for regression cycle 1`)
- `execution-test-cycle-version=`: Test cycle version for the Zephyr execution 
- `execution-attach-evidence=`: Whether to attach evidence to the Zephyr execution (true/false, default: false) - (Only supported for cucumber reports)

### Supported Tags

The following tags can be used in your test scenarios to control ticket creation, linking, and metadata:

| Tag Prefix         | Example Value           | Description                                                                 |
|--------------------|-------------------------|-----------------------------------------------------------------------------|
| `@JIRA-TEST-KEY:`       | `@JIRA-TEST-KEY:PROJ-123`    | Associates the test with an existing Jira issue key.                        |
| `@JIRA-COMPONENT:` | `@JIRA-COMPONENT:API`   | Adds the specified Jira component to the ticket.                            |
| `@JIRA-LABEL:`     | `@JIRA-LABEL:smoke`     | Adds the specified label to the Jira ticket.                                |
| `@JIRA-EPIC:`      | `@JIRA-EPIC:PROJ-456`   | Links the ticket to the specified Jira Epic.                                |
| `@JIRA-NFR:`       | `@JIRA-NFR:PROJ-789`    | Links the ticket to a Non-Functional Requirement (NFR) Jira issue.          |
| `@JIRA-LINK:`      | `@JIRA-LINK:PROJ-321`   | Creates a generic link to another Jira issue.                               |
| `@JIRA-STORY:`     | `@JIRA-STORY:PROJ-654`  | Links the ticket to a Jira Story.                                           |
| `@JIRA-DEFECT:`    | `@JIRA-DEFECT:PROJ-987` | Links the ticket to a Jira Defect.                                          |
| `@JIRA-IGNORE:`    | `@JIRA-IGNORE`          | Prevents ticket creation or update for this test.                           |

- Tags are case-sensitive and must be used exactly as shown.
### Example

```
java -jar zephyr-automation.jar \
  action-type=CREATE_TICKETS \
  process-type=CUCUMBER_JSON_REPORT \
  jira-base-url=https://your-domain.atlassian.net \
  jira-project-id=PROJ \
  jira-default-user=automation@your-domain.com \
  jira-auth-token=your-token \
  base-path=./test-results \
  report-path=./output/report.json
```

## JUnit 5 Support

Zephyr Automation ships with a dedicated JUnit 5 integration so that pure Java tests can emit the same metadata as the JSON-based reporters.

### Available Annotations

Add these annotations to your JUnit 5 test methods (or declaring classes) to mirror the tag behaviour documented above. They live under `uk.hmcts.zephyr.automation.junit5.annotations`.

| Annotation                | Equivalent Tag Prefix | Description |
|---------------------------|------------------------|-------------|
| `@JiraKey("PROJ-123")`     | `JIRA-TEST-KEY:`            | Links the test to an existing Jira issue key; prevents ticket creation when present. |
| `@JiraComponent("API")`   | `JIRA-COMPONENT:`      | Adds the specified component to created/updated tickets. |
| `@JiraLabel("smoke")`     | `JIRA-LABEL:`          | Adds labels to the ticket. |
| `@JiraEpic("PROJ-456")`   | `JIRA-EPIC:`           | Associates the ticket with the given Epic. |
| `@JiraNfr("PROJ-789")`    | `JIRA-NFR:`            | Links the ticket to a Non-Functional Requirement. |
| `@JiraLink("PROJ-321")`   | `JIRA-LINK:`           | Creates a generic Jira issue link. |
| `@JiraStory("PROJ-654")`  | `JIRA-STORY:`          | Links the ticket to a Story. |
| `@JiraDefect("PROJ-987")` | `JIRA-DEFECT:`         | Links the ticket to a Defect. |
| `@JiraIgnore`              | `JIRA-IGNORE`          | Skips ticket creation or updates for the annotated test. |

### ZephyrAutomationExtension

Include the extension to aggregate JUnit 5 execution results into a Zephyr-compatible JSON file:

```java
@ExtendWith(ZephyrAutomationExtension.class)
class MyZephyrEnabledTests {
    // ... your tests ...
}
```

Configuration:

1. Set the report destination via `src/test/resources/junit-platform.properties`:
   ```properties
   zephyr.report.location = target/zephyr-reports/Junit5Report.json
   ```
2. Provide the usual CLI arguments (e.g., `action-type`, `process-type=JUNIT5_JSON_REPORT`, Jira credentials, etc.) when invoking the automation CLI so the generated report is consumed just like Cucumber or Cypress outputs.
3. (Optional) The extension honours the existing annotations and will enrich each captured test with tag-derived metadata before writing the JSON file.

## Building

Build the project using Gradle:

```
./gradlew build
```

## Running Tests

Run all tests:

```
./gradlew test
./gradlew integration
./gradlew functional
./gradlew smoke
```

## Code Style & Checks

Run all style checks:

```
./gradlew runAllStyleChecks
```

## Publishing

To publish version, ensure the version in build.gradle is updated to the desired version. and that you have the necessary credentials configured for publishing to your repository.
Then raise a release on GitHub with the same version number, this will trigger the publish task in the CI pipeline which will publish to the repository.

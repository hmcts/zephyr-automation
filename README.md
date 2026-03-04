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
- `execution-envrionment=`: Environment for the Zephyr execution (e.g., `Staging`, `Production`)
- `execution-build=`:  Build version for the Zephyr execution (e.g., `1.0.0`)

**Required for Action Type: CREATE_TICKETS or UPDATE_TICKETS:**
- `github-repo-base-src-dir=`: Base source directory for the GitHub repository
- `jira-epic-link-custom-field-id=`: Custom field ID for Jira epic links
- `jira-default-components=`: Comma-separated list of default Jira components (e.g., `ComponentA,ComponentB`)

### Supported Tags

The following tags can be used in your test scenarios to control ticket creation, linking, and metadata:

| Tag Prefix        | Example Value          | Description                                                                 |
|-------------------|------------------------|-----------------------------------------------------------------------------|
| `JIRA-KEY:`       | `JIRA-KEY:PROJ-123`    | Associates the test with an existing Jira issue key.                        |
| `JIRA-COMPONENT:` | `JIRA-COMPONENT:API`   | Adds the specified Jira component to the ticket.                            |
| `JIRA-LABEL:`     | `JIRA-LABEL:smoke`     | Adds the specified label to the Jira ticket.                                |
| `JIRA-EPIC:`      | `JIRA-EPIC:PROJ-456`   | Links the ticket to the specified Jira Epic.                                |
| `JIRA-NFR:`       | `JIRA-NFR:PROJ-789`    | Links the ticket to a Non-Functional Requirement (NFR) Jira issue.          |
| `JIRA-LINK:`      | `JIRA-LINK:PROJ-321`   | Creates a generic link to another Jira issue.                               |
| `JIRA-STORY:`     | `JIRA-STORY:PROJ-654`  | Links the ticket to a Jira Story.                                           |
| `JIRA-DEFECT:`    | `JIRA-DEFECT:PROJ-987` | Links the ticket to a Jira Defect.                                          |
| `JIRA-IGNORE`     | `JIRA-IGNORE`          | Prevents ticket creation or update for this test.                           |

- Tags are case-sensitive and must be used exactly as shown.
- For Cucumber, tags should be added to scenarios or features using the `@` symbol (e.g., `@JIRA-KEY:PROJ-123`).
- For Cypress, tags should be included in the test metadata as strings (e.g., `"JIRA-KEY:PROJ-123"`).

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

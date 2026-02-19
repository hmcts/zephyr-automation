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

package uk.hmcts.zephyr.automation;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZephyrAutomation {

    enum TestArgs {
        CUCUMBER_CREATE_TICKETS(new String[]{
            "action-type=CREATE_TICKETS",
            "process-type=CUCUMBER_JSON_REPORT",
            "base-path=/Users/benedwards/Desktop/Projects/opal/github/opal-fines-service/src/functionalTest",
            "report-path=/Users/benedwards/Desktop/Projects/opal/github/opal-fines-service/target/cucumber.json",
            "github-repo-base-src-dir=https://github.com/hmcts/opal-fines-service/tree/master/src/functionalTest"
        }),
        CUCUMBER_CREATE_EXECUTION(new String[]{
            "action-type=CREATE_EXECUTION",
            "process-type=CUCUMBER_JSON_REPORT",
            "base-path=/Users/benedwards/Desktop/Projects/opal/github/opal-fines-service/src/functionalTest",
            "report-path=/Users/benedwards/Desktop/Projects/opal/github/opal-fines-service/target/cucumber.json",
            "github-repo-base-src-dir=https://github.com/hmcts/opal-fines-service/tree/master/src/functionalTest"
        }),
        CYPRESS_CREATE_TICKETS(new String[]{
            "action-type=CREATE_TICKETS",
            "process-type=CYPRESS_JSON_REPORT",
            "base-path=/Users/benedwards/Desktop/Projects/opal/github/opal-frontend",
            "report-path=/Users/benedwards/Desktop/Projects/opal/github/opal-frontend/cypress/reports/zephyr-report"
                + ".json",
            "github-repo-base-src-dir=https://github.com/hmcts/opal-frontend"
        }),
        CYPRESS_CREATE_EXECUTION(new String[]{
            "action-type=CREATE_EXECUTION",
            "process-type=CYPRESS_JSON_REPORT",
            "base-path=/Users/benedwards/Desktop/Projects/opal/github/opal-frontend",
//            "report-path=/Users/benedwards/Desktop/Projects/opal/github/opal-frontend/cypress/reports/zephyr"
//                + "-report.json",
            "report-path=/Users/benedwards/Desktop/Projects/opal/github/zephyr-automation/src/main/java/uk/hmcts/zephyr"
                + "/automation/tmp.json",
            "github-repo-base-src-dir=https://github.com/hmcts/opal-frontend"
        });

        private final String[] args;

        TestArgs(String[] args) {
            String[] baseArgs = new String[]{
                "jira-base-url=https://tools.hmcts.net/jira/rest/api/latest",
                "jira-project-id=33305",
                "jira-default-user=Ben.Edwards",
                "jira-auth-token=Bearer MjAzODc2NDc0NTI5OpAFFYU+sKFv95Tp4JsOuoat6D39",
                "jira-epic-link-custom-field-id=customfield_10008"};
            this.args = new String[baseArgs.length + args.length];
            System.arraycopy(baseArgs, 0, this.args, 0, baseArgs.length);
            System.arraycopy(args, 0, this.args, baseArgs.length, args.length);
        }
    }

    public static void main(String[] args) {
        args = TestArgs.CUCUMBER_CREATE_TICKETS.args;
        Config.instantiate(args);
        Config.printConfig();
        Config.getProcessType().processAction(Config.getActionType());
    }
}
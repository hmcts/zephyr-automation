package uk.hmcts;


import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;

@Slf4j
public class Main {


    private static void parseArgs(String[] args) {
        //Capture the action type and process type from the command line arguments
        for (String arg : args) {
            if (arg.startsWith("action-type=")) {
                Config.actionType = Config.ActionType.valueOf(arg.substring("action-type=".length()));
            } else if (arg.startsWith("process-type=")) {
                Config.processType = Config.ProcessType.valueOf(arg.substring("process-type:".length()));
            } else if (arg.startsWith("base-path=")) {
                Config.basePath = arg.substring("base-path=".length());
            }
        }

        if (Config.actionType == null || Config.processType == null) {
            throw new IllegalArgumentException(
                "Both action-type and process-type must be specified as command line arguments");
        }
    }

    public static void main(String[] args) {
        parseArgs(args);
        Config.processType.processAction(Config.actionType, args);
    }
}
package uk.hmcts;


import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.CreateTickets;
import uk.hmcts.zephyr.automation.cucumber.report.Feature;
import uk.hmcts.zephyr.util.FileUtil;

import java.util.List;

@Slf4j
public class Main {


    private static void parseArgs(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("createTickets")) {
                Config.createTickets = true;
            } else if (arg.equalsIgnoreCase("updateTickets")) {
                Config.updateTickets = true;
            } else if (arg.startsWith("cucumberPath:")) {
                Config.cucumberPath = arg.substring("cucumberPath:".length());
            }
        }
        log.info("Options: createTickets={}, updateTickets={}, cucumberPath={}",
            Config.createTickets,
            Config.updateTickets,
            Config.cucumberPath);
    }

    public static void main(String[] args) {
        parseArgs(args);

        List<Feature> features = FileUtil.readFromFile(Config.cucumberPath, new TypeReference<>() {
        });
        log.info("Features read: {}", features.size());

        if (Config.createTickets) {
            new CreateTickets(features).create();
        }


        // Write the updated features back to the file
//TODO        FileUtil.writeToFile(Config.cucumberPath, features);
    }
}
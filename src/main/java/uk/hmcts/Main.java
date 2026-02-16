package uk.hmcts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.CreateTickets;
import uk.hmcts.zephyr.automation.cucumber.report.Feature;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
public class Main {
    public static List<Feature> readFromFile(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), new TypeReference<>() {
        });
    }

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

        try {
            List<Feature> features = readFromFile(Config.cucumberPath);
            log.info("Features read: {}", features.size());

            if (Config.createTickets) {
                new CreateTickets(features).create();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
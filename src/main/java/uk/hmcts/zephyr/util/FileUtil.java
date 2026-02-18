package uk.hmcts.zephyr.util;

import lombok.SneakyThrows;
import tools.jackson.core.type.TypeReference;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cucumber.models.Feature;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileUtil {

    public static <T> T readFromFile(String filePath, TypeReference<T> typeReference) {
        return Config.OBJECT_MAPPER.readValue(new File(filePath), typeReference);
    }


    public static void writeToFile(String filePath, List<Feature> features) {
        Config.OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), features);
    }

    public static String readFileAsString(String filePath) {
        return Config.OBJECT_MAPPER.readValue(new File(filePath), String.class);
    }

    @SneakyThrows
    public static List<String> readFileAsLines(String filePath) {
        return Files.readAllLines(Paths.get(filePath));
    }

    @SneakyThrows
    public static void saveLines(String filePath, List<String> lines) {
        Files.write(Paths.get(filePath), lines);
    }
}

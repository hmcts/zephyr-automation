package uk.hmcts.zephyr.automation.util;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import uk.hmcts.zephyr.automation.Config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileUtil {

    @SneakyThrows
    public static <T> T readFromFile(String filePath, TypeReference<T> typeReference) {
        return Config.getObjectMapper().readValue(new File(filePath), typeReference);
    }

    @SneakyThrows
    public static void writeToFile(String filePath, Object object) {
        Config.getObjectMapper().writerWithDefaultPrettyPrinter().writeValue(new File(filePath), object);
    }


    @SneakyThrows
    public static List<String> readFileAsLines(String filePath) {
        return Files.readAllLines(Paths.get(filePath));
    }
}

package uk.hmcts.zephyr.util;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import uk.hmcts.zephyr.automation.cucumber.report.Feature;

import java.io.File;
import java.util.List;

public class FileUtil {
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    public static <T> T readFromFile(String filePath, TypeReference<T> typeReference) {
        return objectMapper.readValue(new File(filePath), typeReference);
    }


    public static void writeToFile(String filePath, List<Feature> features) {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), features);
    }
}

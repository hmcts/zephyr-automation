package uk.hmcts.zephyr.util;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cucumber.models.Feature;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class FileUtil {

    public static <T> T readFromFile(String filePath, TypeReference<T> typeReference) {
        return Config.OBJECT_MAPPER.readValue(new File(filePath), typeReference);
    }


    public static void writeToFile(String filePath, List<Feature> features) {
        Config.OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), features);
    }
}

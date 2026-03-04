package uk.hmcts.zephyr.automation.util;

import lombok.SneakyThrows;
import uk.hmcts.zephyr.automation.Config;

import java.util.Collection;

public class Util {

    @SneakyThrows
    public static String writeObjectToString(Object object) {
        return Config.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    public static boolean hasItems(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
}

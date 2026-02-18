package uk.hmcts.zephyr.automation.util;

import lombok.SneakyThrows;
import uk.hmcts.zephyr.automation.Config;

public class Util {

    @SneakyThrows
    public static String writeObjectToString(Object object) {
        return Config.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}

package uk.hmcts.zephyr.automation.support;

import uk.hmcts.zephyr.automation.zephyr.ZephyrImpl;

import java.lang.reflect.Field;

public class TestUtil {

    public static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = ZephyrImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

package uk.hmcts.zephyr.automation.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.hmcts.zephyr.automation.Config;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilTest {

    @Test
    @DisplayName("writeObjectToString uses Config's ObjectMapper")
    void writeObjectToString_usesConfigObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        try (MockedStatic<Config> configMock = Mockito.mockStatic(Config.class)) {
            configMock.when(Config::getObjectMapper).thenReturn(mapper);

            String json = Util.writeObjectToString(Map.of("a", 1));

            assertTrue(json.contains("\"a\""));
            assertTrue(json.contains("1"));
        }
    }

    @Test
    void hasItems_returnsFalseForNull() {
        assertFalse(Util.hasItems(null));
    }

    @Test
    void hasItems_returnsFalseForEmptyCollection() {
        assertFalse(Util.hasItems(java.util.Collections.emptyList()));
    }

    @Test
    void hasItems_returnsTrueForNonEmptyCollection() {
        assertTrue(Util.hasItems(java.util.List.of(1, 2, 3)));
    }
}


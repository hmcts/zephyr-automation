package uk.hmcts.zephyr.automation.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.hmcts.zephyr.automation.Config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FileUtilTest {

    @Test
    void readFromFile_readsJsonUsingConfigMapper() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Path tempFile = Files.createTempFile("zephyr-automation", ".json");
        mapper.writeValue(tempFile.toFile(), Map.of("a", 1));

        try (MockedStatic<Config> configMock = Mockito.mockStatic(Config.class)) {
            configMock.when(Config::getObjectMapper).thenReturn(mapper);

            Map<String, Object> value = FileUtil.readFromFile(tempFile.toString(), new TypeReference<>() {
            });

            assertNotNull(value);
            assertEquals(1, ((Number) value.get("a")).intValue());
        }
    }

    @Test
    void writeToFile_writesJsonUsingConfigMapper() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Path tempFile = Files.createTempFile("zephyr-automation", ".json");

        try (MockedStatic<Config> configMock = Mockito.mockStatic(Config.class)) {
            configMock.when(Config::getObjectMapper).thenReturn(mapper);

            FileUtil.writeToFile(tempFile.toString(), Map.of("b", "value"));
        }

        Map<String, Object> readBack = mapper.readValue(tempFile.toFile(), new TypeReference<>() {
        });
        assertEquals("value", readBack.get("b"));
    }

    @Test
    void readFileAsLines_readsAllLines() throws Exception {
        Path tempFile = Files.createTempFile("zephyr-automation", ".txt");
        Files.write(tempFile, List.of("first", "second"));

        List<String> lines = FileUtil.readFileAsLines(tempFile.toString());

        assertEquals(List.of("first", "second"), lines);
    }
}


package uk.hmcts.zephyr.automation.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

class SampleParameterizedJiraKeysTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @JiraTestKey(value = "ABC-101", name = "false")
    @JiraTestKey(value = "ABC-202", name = "true")
    void parameterizedMethod(boolean consolidation) {
    }
}



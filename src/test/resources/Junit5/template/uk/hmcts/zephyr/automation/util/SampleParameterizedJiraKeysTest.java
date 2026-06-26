package uk.hmcts.zephyr.automation.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SampleParameterizedJiraKeysTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void parameterizedMethod(boolean consolidation) {
    }
}



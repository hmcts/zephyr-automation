package uk.hmcts.zephyr.automation.jira.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JiraIssueFieldsWrapperTest {

    @Test
    void setSummary_removesNewLinesAndTruncatesTo255Characters() {
        StringBuilder builder = new StringBuilder("Line one\nLine two\r");
        for (int i = 0; i < 300; i++) {
            builder.append('x');
        }

        JiraIssueFieldsWrapper.Fields fields = JiraIssueFieldsWrapper.Fields.builder().build();

        fields.setSummary(builder.toString());

        String summary = fields.getSummary();
        assertEquals(255, summary.length());
        org.junit.jupiter.api.Assertions.assertFalse(summary.contains("\n"));
        org.junit.jupiter.api.Assertions.assertFalse(summary.contains("\r"));
        assertEquals("Line one Line two ", summary.substring(0, "Line one Line two ".length()));
    }

    @Test
    void setSummary_handlesNullInput() {
        JiraIssueFieldsWrapper.Fields fields = JiraIssueFieldsWrapper.Fields.builder().build();

        fields.setSummary(null);

        assertNull(fields.getSummary());
    }
}


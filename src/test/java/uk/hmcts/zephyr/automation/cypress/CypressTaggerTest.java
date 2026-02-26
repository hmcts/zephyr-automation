package uk.hmcts.zephyr.automation.cypress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CypressTaggerTest {

    private static final String SAMPLE_RESOURCE = "cypress/sample-test.ts";
    private static final String NESTED_RESOURCE = "cypress/nested-suite.cy.ts";
    private static final String EXPECTED_SAMPLE_WITH_SMOKE = "cypress/expected/sample-test-with-smoke.ts";
    private static final String EXPECTED_SAMPLE_WITH_REGRESSION = "cypress/expected/sample-test-with-regression.ts";
    private static final String EXPECTED_NESTED_WITH_NESTED_TAG = "cypress/expected/nested-suite-with-nested-tag.cy.ts";
    private static final String EXPECTED_NESTED_WITH_CHAINED_TAG = "cypress/expected/nested-suite-with-chained-tag.cy.ts";
    private static final String EXPECTED_NESTED_WITH_MULTILINE_TAG = "cypress/expected/nested-suite-with-multiline-tag.cy.ts";
    private String sampleContent;
    private String nestedContent;

    @BeforeEach
    void setUp() throws IOException {
        sampleContent = readResource(SAMPLE_RESOURCE);
        nestedContent = readResource(NESTED_RESOURCE);
    }

    @Nested
    class AddTagToCypressTest {

        @Test
        void givenMatchingTitleWithExistingTags_whenAddTag_thenAppendsTagOnce() throws IOException {
            assertFixture(sampleContent, EXPECTED_SAMPLE_WITH_SMOKE, "user logs in", "@SMOKE");
        }

        @Test
        void givenTitleWithoutTags_whenAddTag_thenCreatesTagsArray() throws IOException {
            assertFixture(sampleContent, EXPECTED_SAMPLE_WITH_REGRESSION, "user logs out", "@REGRESSION");
        }

        @Test
        void givenExistingTag_whenAddTag_thenLeavesContentUnchanged() throws IOException {
            String updated = assertFixture(sampleContent, EXPECTED_SAMPLE_WITH_SMOKE, "user logs in", "@SMOKE");
            String secondPass = CypressTagger.addTagToCypressTestInContent(updated, "user logs in", "@SMOKE");

            assertEquals(normalize(updated), normalize(secondPass));
        }

        @Test
        void givenNestedDescribe_whenAddTag_thenUpdatesInnerTestOnly() throws IOException {
            assertFixture(nestedContent, EXPECTED_NESTED_WITH_NESTED_TAG, "logs out automatically", "@NESTED");
        }

        @Test
        void givenConcatenatedTitle_whenAddTag_thenResolvesExactTitle() throws IOException {
            assertFixture(nestedContent, EXPECTED_NESTED_WITH_CHAINED_TAG, "deep nest target", "@CHAINED");
        }

        @Test
        void givenMultiLineConcatenatedTitle_whenAddTag_thenHandlesWhitespaceBetweenSegments() throws IOException {
            assertFixture(nestedContent, EXPECTED_NESTED_WITH_MULTILINE_TAG, "multi line title", "@MULTILINE");
        }
    }

    private String assertFixture(String sourceContent, String expectedResource, String title, String newTag)
        throws IOException {
        String updated = CypressTagger.addTagToCypressTestInContent(sourceContent, title, newTag);
        assertEquals(normalize(readResource(expectedResource)), normalize(updated));
        return updated;
    }

    private String readResource(String resourcePath) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String normalize(String content) {
        return content.replace("\r\n", "\n").stripTrailing();
    }
}

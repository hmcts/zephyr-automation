package uk.hmcts.zephyr.automation.cucumber.models;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Element.Step.Embedding;
import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature.Tag;
import uk.hmcts.zephyr.automation.support.CucumberDataUtil;
import uk.hmcts.zephyr.automation.support.TestUtil;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CucumberFeatureTest {

    @BeforeEach
    void setUp() throws Exception {
        TestUtil.resetSingletons();
        Config.instantiate(TestUtil.defaultArgs(
            Config.ActionType.CREATE_TICKETS,
            Config.ProcessType.CUCUMBER_JSON_REPORT
        ));
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtil.resetSingletons();
    }

    @Nested
    class GetTagsTest {

        @Test
        void given_nullTags_when_getTags_then_initializesAndCachesList() {
            Element element = new Element();
            element.setTags(null);

            List<Tag> firstCall = element.getTags();
            List<Tag> secondCall = element.getTags();

            assertNotNull(firstCall);
            assertSame(firstCall, secondCall);
        }
    }

    @Nested
    class AddTagTest {

        @Test
        void given_tag_when_addTag_then_appendsToList() {
            Element element = new Element();
            Tag tag = new Tag();
            tag.setName("@JIRA-123");

            element.addTag(tag);

            assertTrue(element.getTags().contains(tag));
        }
    }

    @Nested
    class HasTagTest {

        @Test
        void given_existingTag_when_hasTag_then_returnsTrue() {
            Element element = new Element();
            Tag tag = new Tag();
            tag.setName("@link");
            element.addTag(tag);

            assertTrue(element.hasTag("@link"));
        }

        @Test
        void given_missingTag_when_hasTag_then_returnsFalse() {
            Element element = new Element();
            Tag tag = new Tag();
            tag.setName("@existing");
            element.addTag(tag);

            assertFalse(element.hasTag("@missing"));
        }
    }

    @Nested
    class GitHubLinkTest {

        @Test
        void given_classpathUri_when_getGitHubLink_then_mapsToRepoPath() {
            Element element = elementWithFeature("Feature", "classpath:features/sample.feature", 12);

            String link = element.getGitHubLink();

            assertEquals("/repo/resources/features/sample.feature#L12", link);
        }
    }

    @Nested
    class LocationDisplayNameTest {

        @Test
        void given_feature_when_getLocationDisplayName_then_returnsFeatureName() {
            Element element = elementWithFeature("Payments feature", "classpath:features/payments.feature", 5);

            assertEquals("Payments feature", element.getLocationDisplayName());
        }
    }

    @Nested
    class NameAndLocationTest {

        @Test
        void given_element_when_getNameAndLocation_then_formatsString() {
            Element element = elementWithFeature("Auth feature", "classpath:features/auth.feature", 8);
            element.setName("Successful login");

            assertEquals("Successful login (line 8 in feature classpath:features/auth.feature)",
                element.getNameAndLocation());
        }
    }

    @Nested
    class ZephyrExecutionStatusTest {

        @Test
        void given_allStepsPassed_when_getZephyrExecutionStatus_then_returnsPass() {
            Element element = elementWithFeature("Feature", "classpath:features/file.feature", 1);
            element.setSteps(List.of(CucumberDataUtil.step("passed"), CucumberDataUtil.step("PASSED")));

            assertEquals(ZephyrConstants.ExecutionStatus.PASS, element.getZephyrExecutionStatus());
        }

        @Test
        void given_anyStepFailed_when_getZephyrExecutionStatus_then_returnsFail() {
            Element element = elementWithFeature("Feature", "classpath:features/file.feature", 1);
            element.setSteps(List.of(CucumberDataUtil.step("passed"), CucumberDataUtil.step("failed")));

            assertEquals(ZephyrConstants.ExecutionStatus.FAIL, element.getZephyrExecutionStatus());
        }

        @Test
        void given_noPassOrFail_when_getZephyrExecutionStatus_then_returnsUnexecuted() {
            Element element = elementWithFeature("Feature", "classpath:features/file.feature", 1);
            element.setSteps(List.of(CucumberDataUtil.step("skipped")));

            assertEquals(ZephyrConstants.ExecutionStatus.UNEXECUTED, element.getZephyrExecutionStatus());
        }
    }


    @Nested
    @DisplayName("Embedding tests")
    class EmbeddingTests {
        @Test
        void given_base64Data_when_getContent_then_decodesBytes() {
            String original = "hello world";
            String encoded = Base64.getEncoder().encodeToString(original.getBytes(StandardCharsets.UTF_8));
            Embedding embedding = new Embedding("image/png", encoded);

            assertEquals(original, new String(embedding.getContent(), StandardCharsets.UTF_8));
        }

        @Test
        void given_mimeType_when_getFileName_then_usesExtension() {
            Embedding embedding = new Embedding("image/jpeg", "ZGF0YQ==");

            assertEquals("embedding.jpeg", embedding.getFileName());
        }

        @Test
        void given_mimeType_when_getContentType_then_returnsSameValue() {
            Embedding embedding = new Embedding("image/gif", "ZGF0YQ==");

            assertEquals("image/gif", embedding.getContentType());
        }
    }

    private Element elementWithFeature(String featureName, String uri, int line) {
        CucumberFeature feature = new CucumberFeature();
        feature.setName(featureName);
        feature.setUri(uri);
        Element element = new Element();
        element.setCucumberFeature(feature);
        element.setLine(line);
        element.setName("Scenario");
        return element;
    }
}

package uk.hmcts.zephyr.automation.cypress.models;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.support.TestUtil;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CypressReportTest {

    @AfterEach
    void tearDown() throws Exception {
        TestUtil.resetSingletons();
    }

    private CypressReport.CypressTest newTest() {
        return new CypressReport.CypressTest();
    }

    @Nested
    class TagsTest {

        @Test
        void givenNoTags_whenGetTags_thenReturnsEmptyMutableList() {
            CypressReport.CypressTest cypressTest = newTest();

            List<String> tags = cypressTest.getTags();

            assertNotNull(tags);
            assertTrue(tags.isEmpty());

            cypressTest.addTag("smoke");

            assertSame(tags, cypressTest.getTags());
            assertEquals(List.of("smoke"), tags);
        }

        @Test
        void givenTags_whenGetTags_thenReturnsTheTags() {
            CypressReport.CypressTest cypressTest = newTest();
            cypressTest.addTag("smoke1");
            cypressTest.addTag("smoke2");
            cypressTest.addTag("smoke3");
            List<String> tags = cypressTest.getTags();

            assertNotNull(tags);
            assertFalse(tags.isEmpty());
            assertSame(tags, cypressTest.getTags());
            assertEquals(List.of("smoke1", "smoke2", "smoke3"), tags);
        }

        @Test
        void givenNoTags_whenAddTag_thenInitializesListAndAddsTag() {
            CypressReport.CypressTest cypressTest = newTest();
            cypressTest.addTag("smoke");

            List<String> tags = cypressTest.getTags();

            assertNotNull(tags);
            assertEquals(List.of("smoke"), tags);
        }


        @Test
        void givenTags_whenHasTag_thenDetectsPresence() {
            CypressReport.CypressTest cypressTest = newTest();
            cypressTest.addTag("smoke");

            assertTrue(cypressTest.hasTag("smoke"));
            assertFalse(cypressTest.hasTag("regression"));
        }
    }

    @Nested
    class GetLocationDisplayNameTest {

        @Test
        void givenParents_whenGetLocationDisplayName_thenJoinsWithChevron() {
            CypressReport.CypressTest cypressTest = newTest();
            cypressTest.setParents(List.of("Login", "Successful path"));

            String displayName = cypressTest.getLocationDisplayName();

            assertEquals("Login > Successful path", displayName);
        }

        @Test
        void givenNoParents_whenGetLocationDisplayName_thenThrows() {
            CypressReport.CypressTest cypressTest = newTest();

            //Check null parents
            RuntimeException exception = assertThrows(RuntimeException.class, cypressTest::getLocationDisplayName);
            assertEquals("No parents", exception.getMessage());

            //Check empty parents
            cypressTest.setParents(new ArrayList<>());
            exception = assertThrows(RuntimeException.class, cypressTest::getLocationDisplayName);
            assertEquals("No parents", exception.getMessage());

        }
    }

    @Nested
    class GetGitHubLinkTest {

        @BeforeEach
        void setUpConfig() throws Exception {
            TestUtil.resetSingletons();
            Config.instantiate(TestUtil.defaultArgs(Config.ActionType.CREATE_TICKETS,
                Config.ProcessType.CYPRESS_JSON_REPORT));
        }

        @Test
        void givenFile_whenGetGitHubLink_thenPrefixesConfiguredBase() {
            CypressReport.CypressTest cypressTest = newTest();
            cypressTest.setFile("cypress/e2e/login.cy.js");

            assertEquals("/repo/cypress/e2e/login.cy.js", cypressTest.getGitHubLink());
        }
    }

    @Nested
    class GetNameAndLocationTest {

        @Test
        void givenTitleAndParents_whenGetNameAndLocation_thenFormatsOutput() {
            CypressReport.CypressTest cypressTest = newTest();
            cypressTest.setTitle("User logs in");
            cypressTest.setParents(List.of("Login", "Successful path"));

            assertEquals("User logs in (Login > Successful path)", cypressTest.getNameAndLocation());
        }
    }

    @Nested
    class GetZephyrExecutionStatusTest {

        @Test
        void givenPassedStatus_whenGetZephyrExecutionStatus_thenReturnsPass() {
            CypressReport.CypressTest cypressTest = newTest();
            cypressTest.setStatus("passed");

            assertEquals(ZephyrConstants.ExecutionStatus.PASS, cypressTest.getZephyrExecutionStatus());
        }

        @Test
        void givenFailedStatus_whenGetZephyrExecutionStatus_thenReturnsFail() {
            CypressReport.CypressTest cypressTest = newTest();
            cypressTest.setStatus("failed");

            assertEquals(ZephyrConstants.ExecutionStatus.FAIL, cypressTest.getZephyrExecutionStatus());
        }

        @Test
        void givenPendingStatus_whenGetZephyrExecutionStatus_thenReturnsUnexecuted() {
            CypressReport.CypressTest cypressTest = newTest();
            cypressTest.setStatus("pending");

            assertEquals(ZephyrConstants.ExecutionStatus.UNEXECUTED, cypressTest.getZephyrExecutionStatus());
        }

        @Test
        void givenSkippedStatus_whenGetZephyrExecutionStatus_thenReturnsUnexecuted() {
            CypressReport.CypressTest cypressTest = newTest();
            cypressTest.setStatus("skipped");

            assertEquals(ZephyrConstants.ExecutionStatus.UNEXECUTED, cypressTest.getZephyrExecutionStatus());
        }

        @Test
        void givenUnknownStatus_whenGetZephyrExecutionStatus_thenReturnsUnexecuted() {
            CypressReport.CypressTest cypressTest = newTest();
            cypressTest.setStatus("undefined");

            assertEquals(ZephyrConstants.ExecutionStatus.UNEXECUTED, cypressTest.getZephyrExecutionStatus());
        }
    }
}


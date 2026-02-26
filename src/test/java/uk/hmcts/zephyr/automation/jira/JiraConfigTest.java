package uk.hmcts.zephyr.automation.jira;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JiraConfigTest {

    @BeforeEach
    void resetSingleton() throws Exception {
        Field instanceField = JiraConfig.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    void given_completeArguments_when_instantiate_then_valuesAreParsedAndExposed() {
        String[] args = {
            "jira-base-url=https://example.atlassian.net",
            "jira-project-id=OPAL",
            "jira-default-user=opal.user",
            "jira-auth-token=token-123",
            "jira-epic-link-custom-field-id=custom_42",
            "jira-default-components= Core , API,  UI "
        };

        JiraConfig.instantiate(args);

        assertEquals("https://example.atlassian.net", JiraConfig.getBaseUrl());
        assertEquals("OPAL", JiraConfig.getProjectId());
        assertEquals("opal.user", JiraConfig.getDefaultUser());
        assertEquals("token-123", JiraConfig.getAuthToken());
        assertEquals("custom_42", JiraConfig.getEpicLinkCustomFieldId());
        assertEquals(List.of("Core", "API", "UI"), JiraConfig.getDefaultComponents());
    }

    @Test
    void given_missingRequiredArguments_when_instantiate_then_throwsIllegalArgumentException() {
        String[] args = {
            "jira-base-url=https://example.atlassian.net",
            "jira-project-id=OPAL",
            "jira-default-user=opal.user"
        };

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> JiraConfig.instantiate(args));

        assertEquals(
            "Jira configuration requires jira-base-url, jira-project-id, jira-default-user, and jira-auth-token to be"
                + " specified as command line arguments",
            exception.getMessage());
    }

    @Test
    void given_alreadyInstantiated_when_instantiate_then_throwsIllegalStateException() {
        String[] args = {
            "jira-base-url=https://example.atlassian.net",
            "jira-project-id=OPAL",
            "jira-default-user=opal.user",
            "jira-auth-token=token-123"
        };

        JiraConfig.instantiate(args);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> JiraConfig.instantiate(args));
        assertEquals("JiraConfig has already been instantiated", exception.getMessage());
    }

    @Test
    void given_noEpicLinkCustomFieldId_when_instantiate_then_epicLinkFieldIsNull() {
        String[] args = {
            "jira-base-url=https://example.atlassian.net",
            "jira-project-id=OPAL",
            "jira-default-user=opal.user",
            "jira-auth-token=token-123"
        };

        JiraConfig.instantiate(args);

        assertNull(JiraConfig.getEpicLinkCustomFieldId());
    }

    @Test
    void given_defaultComponents_when_instantiate_then_listIsUnmodifiable() {
        String[] args = {
            "jira-base-url=https://example.atlassian.net",
            "jira-project-id=OPAL",
            "jira-default-user=opal.user",
            "jira-auth-token=token-123",
            "jira-default-components=Core"
        };

        JiraConfig.instantiate(args);

        List<String> components = JiraConfig.getDefaultComponents();
        assertThrows(UnsupportedOperationException.class, () -> components.add("API"));
    }
}


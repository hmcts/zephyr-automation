package uk.hmcts.zephyr.automation.jira;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.hmcts.zephyr.automation.jira.client.JiraClient;
import uk.hmcts.zephyr.automation.jira.models.JiraComponent;
import uk.hmcts.zephyr.automation.jira.models.JiraIssue;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueFieldsWrapper;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueLink;
import uk.hmcts.zephyr.automation.jira.models.JiraSearchRequest;
import uk.hmcts.zephyr.automation.jira.models.JiraSearchResponse;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JiraImplTest {

    @Test
    void given_projectComponentsCached_when_getProjectComponents_then_clientCalledOnceAndCached() throws Exception {
        JiraClient jiraClient = mock(JiraClient.class);
        JiraImpl jira = createSubjectWithMock(jiraClient);

        List<JiraComponent> components = List.of(componentNamed("Core"));
        when(jiraClient.getProjectComponents("OPAL")).thenReturn(components);

        List<JiraComponent> firstCall = jira.getProjectComponents("OPAL");
        List<JiraComponent> secondCall = jira.getProjectComponents("OPAL");

        assertSame(components, firstCall);
        assertSame(components, secondCall);
        verify(jiraClient, times(1)).getProjectComponents("OPAL");
    }

    @Test
    void given_componentNameMatchesIgnoringCase_when_getComponentByName_then_returnsComponent() throws Exception {
        JiraClient jiraClient = mock(JiraClient.class);
        JiraImpl jira = createSubjectWithMock(jiraClient);

        JiraComponent component = componentNamed("Core");
        when(jiraClient.getProjectComponents("OPAL")).thenReturn(List.of(component));

        JiraComponent result = jira.getComponentByName("OPAL", "core");

        assertSame(component, result);
    }

    @Test
    void given_componentNameMissing_when_getComponentByName_then_returnsNull() throws Exception {
        JiraClient jiraClient = mock(JiraClient.class);
        JiraImpl jira = createSubjectWithMock(jiraClient);

        when(jiraClient.getProjectComponents("OPAL")).thenReturn(List.of(componentNamed("Core")));

        JiraComponent result = jira.getComponentByName("OPAL", "api");

        assertNull(result);
    }

    @Test
    void given_issuePayload_when_createIssue_then_delegatesToClient() throws Exception {
        JiraClient jiraClient = mock(JiraClient.class);
        JiraImpl jira = createSubjectWithMock(jiraClient);

        JiraIssueFieldsWrapper request = mock(JiraIssueFieldsWrapper.class);
        JiraIssue response = mock(JiraIssue.class);
        when(jiraClient.createIssue(request)).thenReturn(response);

        JiraIssue result = jira.createIssue(request);

        assertSame(response, result);
        verify(jiraClient).createIssue(request);
    }

    @Test
    void given_issueLink_when_linkIssue_then_delegatesToClient() throws Exception {
        JiraClient jiraClient = mock(JiraClient.class);
        JiraImpl jira = createSubjectWithMock(jiraClient);

        JiraIssueLink link = mock(JiraIssueLink.class);

        jira.linkIssue(link);

        verify(jiraClient).linkIssue(link);
    }

    @Test
    void given_searchRequest_when_searchIssues_then_delegatesToClient() throws Exception {
        JiraClient jiraClient = mock(JiraClient.class);
        JiraImpl jira = createSubjectWithMock(jiraClient);

        JiraSearchRequest request = mock(JiraSearchRequest.class);
        JiraSearchResponse response = mock(JiraSearchResponse.class);
        when(jiraClient.searchIssues(request)).thenReturn(response);

        JiraSearchResponse result = jira.searchIssues(request);

        assertSame(response, result);
        verify(jiraClient).searchIssues(request);
    }

    @Test
    void given_updatePayload_when_updateIssue_then_delegatesToClient() throws Exception {
        JiraClient jiraClient = mock(JiraClient.class);
        JiraImpl jira = createSubjectWithMock(jiraClient);

        JiraIssueFieldsWrapper request = mock(JiraIssueFieldsWrapper.class);
        JiraIssue response = mock(JiraIssue.class);
        when(jiraClient.updateIssue(request, "ID-1")).thenReturn(response);

        JiraIssue result = jira.updateIssue(request, "ID-1");

        assertSame(response, result);
        verify(jiraClient).updateIssue(request, "ID-1");
    }

    private JiraImpl createSubjectWithMock(JiraClient jiraClient) throws Exception {
        JiraImpl jira = new JiraImpl(new ObjectMapper(), "http://localhost", "Bearer token");
        setField(jira, "jiraClient", jiraClient);
        setField(jira, "componentsCacheMap", new HashMap<>());
        return jira;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = JiraImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static JiraComponent componentNamed(String name) {
        JiraComponent component = new JiraComponent();
        component.setName(name);
        return component;
    }
}


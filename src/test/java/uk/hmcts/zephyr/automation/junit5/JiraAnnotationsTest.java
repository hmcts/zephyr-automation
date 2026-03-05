package uk.hmcts.zephyr.automation.junit5;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraComponent;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraIgnore;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraKey;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraLink;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JiraAnnotationsTest {

    @Test
    void mergesClassAndMethodAnnotationsWithoutDuplicates() throws NoSuchMethodException {
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Method method = AnnotatedTestCase.class.getDeclaredMethod("annotatedMethod");

        Mockito.when(context.getTestClass()).thenReturn(Optional.of(AnnotatedTestCase.class));
        Mockito.when(context.getTestMethod()).thenReturn(Optional.of(method));

        JiraAnnotationMetadata metadata = JiraAnnotations.fromContext(context);

        assertEquals(Set.of("METHOD-KEY"), metadata.getJiraKey());
        assertEquals(Set.of("CLASS-LINK", "METHOD-LINK-1", "METHOD-LINK-2"), metadata.getJiraLinks());
        assertEquals(Set.of("payments", "workflow"), metadata.getJiraComponents());
        assertTrue(metadata.isJiraIgnore());
    }

    @JiraLink("CLASS-LINK")
    @JiraComponent("payments")
    static class AnnotatedTestCase {

        @JiraKey("METHOD-KEY")
        @JiraLink("METHOD-LINK-1")
        @JiraLink("METHOD-LINK-2")
        @JiraComponent("workflow")
        @JiraComponent("payments")
        @JiraIgnore
        void annotatedMethod() {
        }
    }
}

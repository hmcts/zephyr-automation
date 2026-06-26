package uk.hmcts.zephyr.automation.junit5;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraComponent;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraIgnore;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraLink;

import java.lang.reflect.Method;
import java.util.List;
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

    @Test
    void givenMatchingParameterizedArguments_whenFromContext_thenReturnsMatchingJiraKey() throws NoSuchMethodException {
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Method method = AnnotatedTestCase.class.getDeclaredMethod("parameterizedMethod");

        Mockito.when(context.getTestClass()).thenReturn(Optional.of(AnnotatedTestCase.class));
        Mockito.when(context.getTestMethod()).thenReturn(Optional.of(method));

        JiraAnnotationMetadata metadata = JiraAnnotations.fromContext(context, List.of("alpha", "42"));

        assertEquals(Set.of("PARAM-KEY"), metadata.getJiraKey());
    }

    @Test
    void givenNonMatchingParameterizedArguments_whenFromContext_thenReturnsNoJiraKey() throws NoSuchMethodException {
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Method method = AnnotatedTestCase.class.getDeclaredMethod("parameterizedMethod");

        Mockito.when(context.getTestClass()).thenReturn(Optional.of(AnnotatedTestCase.class));
        Mockito.when(context.getTestMethod()).thenReturn(Optional.of(method));

        JiraAnnotationMetadata metadata = JiraAnnotations.fromContext(context, List.of("other", "42"));

        assertEquals(Set.of(), metadata.getJiraKey());
    }

    @Test
    void givenFallbackAndParameterizedKeys_whenFromContextWithArguments_thenUsesOnlyMatchingParameterizedKey()
        throws NoSuchMethodException {
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Method method = AnnotatedTestCase.class.getDeclaredMethod("parameterizedMethodWithFallbackKey");

        Mockito.when(context.getTestClass()).thenReturn(Optional.of(AnnotatedTestCase.class));
        Mockito.when(context.getTestMethod()).thenReturn(Optional.of(method));

        JiraAnnotationMetadata metadata = JiraAnnotations.fromContext(context, List.of("alpha", "42"));

        assertEquals(Set.of("PARAM-KEY-ONLY"), metadata.getJiraKey());
    }

    @JiraLink("CLASS-LINK")
    @JiraComponent("payments")
    static class AnnotatedTestCase {

        @JiraTestKey("METHOD-KEY")
        @JiraLink("METHOD-LINK-1")
        @JiraLink("METHOD-LINK-2")
        @JiraComponent("workflow")
        @JiraComponent("payments")
        @JiraIgnore
        void annotatedMethod() {
            // Annotation fixture method used only for reflection-based metadata tests.
        }

        @JiraTestKey(value = "PARAM-KEY", arguments = {"alpha", "42"})
        void parameterizedMethod() {
            // Annotation fixture method used only for reflection-based metadata tests.
        }

        @JiraTestKey("FALLBACK-KEY")
        @JiraTestKey(value = "PARAM-KEY-ONLY", arguments = {"alpha", "42"})
        void parameterizedMethodWithFallbackKey() {
            // Annotation fixture method used only for reflection-based metadata tests.
        }
    }
}

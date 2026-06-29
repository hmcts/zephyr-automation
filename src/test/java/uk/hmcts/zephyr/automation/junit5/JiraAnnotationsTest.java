package uk.hmcts.zephyr.automation.junit5;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraComponent;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraIgnore;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraLink;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;
import uk.hmcts.zephyr.automation.junit5.model.Junit5ZephyrReport;

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

        JiraAnnotationMetadata metadata = JiraAnnotations.fromContext(context, Junit5ZephyrReport.Test.Type.STANDARD);

        assertEquals(Set.of("METHOD-KEY"), metadata.getJiraKey());
        assertEquals(Set.of("CLASS-LINK", "METHOD-LINK-1", "METHOD-LINK-2"), metadata.getJiraLinks());
        assertEquals(Set.of("payments", "workflow"), metadata.getJiraComponents());
        assertTrue(metadata.isJiraIgnore());
    }

    @Test
    void givenMatchingParameterizedName_whenFromContext_thenReturnsMatchingJiraKey() throws NoSuchMethodException {
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Method method = AnnotatedTestCase.class.getDeclaredMethod("parameterizedMethod");

        Mockito.when(context.getTestClass()).thenReturn(Optional.of(AnnotatedTestCase.class));
        Mockito.when(context.getTestMethod()).thenReturn(Optional.of(method));
        Mockito.when(context.getDisplayName()).thenReturn("alpha-42");

        JiraAnnotationMetadata metadata =
            JiraAnnotations.fromContext(context, Junit5ZephyrReport.Test.Type.PARAMETERIZED);

        assertEquals(Set.of("PARAM-KEY"), metadata.getJiraKey());
    }

    @Test
    void givenNonMatchingParameterizedName_whenFromContext_thenReturnsNoJiraKey() throws NoSuchMethodException {
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Method method = AnnotatedTestCase.class.getDeclaredMethod("parameterizedMethod");

        Mockito.when(context.getTestClass()).thenReturn(Optional.of(AnnotatedTestCase.class));
        Mockito.when(context.getTestMethod()).thenReturn(Optional.of(method));
        Mockito.when(context.getDisplayName()).thenReturn("different-case");

        JiraAnnotationMetadata metadata =
            JiraAnnotations.fromContext(context, Junit5ZephyrReport.Test.Type.PARAMETERIZED);

        assertEquals(Set.of(), metadata.getJiraKey());
    }

    @Test
    void givenMultipleParameterizedKeys_whenFromContextWithName_thenUsesOnlyMatchingParameterizedKey()
        throws NoSuchMethodException {
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Method method = AnnotatedTestCase.class.getDeclaredMethod("parameterizedMethodWithFallbackKey");

        Mockito.when(context.getTestClass()).thenReturn(Optional.of(AnnotatedTestCase.class));
        Mockito.when(context.getTestMethod()).thenReturn(Optional.of(method));
        Mockito.when(context.getDisplayName()).thenReturn("case-b");

        JiraAnnotationMetadata metadata =
            JiraAnnotations.fromContext(context, Junit5ZephyrReport.Test.Type.PARAMETERIZED);

        assertEquals(Set.of("PARAM-KEY-B"), metadata.getJiraKey());
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

        @JiraTestKey(value = "PARAM-KEY", name = "alpha-42")
        void parameterizedMethod() {
            // Annotation fixture method used only for reflection-based metadata tests.
        }

        @JiraTestKey(value = "PARAM-KEY-A", name = "case-a")
        @JiraTestKey(value = "PARAM-KEY-B", name = "case-b")
        void parameterizedMethodWithFallbackKey() {
            // Annotation fixture method used only for reflection-based metadata tests.
        }
    }
}

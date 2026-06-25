package uk.hmcts.zephyr.automation.junit5;

import org.junit.jupiter.api.extension.ExtensionContext;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraComponent;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraDefect;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraIgnore;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraLabel;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraLink;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraNfr;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class JiraAnnotations {

    private JiraAnnotations() {
    }

    public static JiraAnnotationMetadata fromContext(ExtensionContext context) {
        return fromContext(context, List.of());
    }

    public static JiraAnnotationMetadata fromContext(ExtensionContext context, List<String> invocationArguments) {
        Class<?> testClass = context.getTestClass().orElse(null);
        Method testMethod = context.getTestMethod().orElse(null);

        return new JiraAnnotationMetadata(
            collectJiraKeys(testClass, testMethod, invocationArguments),
            collectValues(testClass, testMethod, JiraComponent.class, JiraComponent::value, true),
            collectValues(testClass, testMethod, JiraLabel.class, JiraLabel::value, true),
            collectValues(testClass, testMethod, JiraEpic.class, JiraEpic::value, true),
            collectValues(testClass, testMethod, JiraNfr.class, JiraNfr::value, true),
            collectValues(testClass, testMethod, JiraLink.class, JiraLink::value, true),
            collectValues(testClass, testMethod, JiraStory.class, JiraStory::value, true),
            collectValues(testClass, testMethod, JiraDefect.class, JiraDefect::value, true),
            !collectValues(testClass, testMethod, JiraIgnore.class, jiraIgnore -> null, false).isEmpty()
        );
    }

    private static Set<String> collectJiraKeys(Class<?> testClass, Method testMethod,
                                               List<String> invocationArguments) {
        Set<String> keys = new LinkedHashSet<>();
        addJiraKeys(keys, testClass, invocationArguments);
        addJiraKeys(keys, testMethod, invocationArguments);
        return keys;
    }

    private static void addJiraKeys(Set<String> keys, AnnotatedElement element, List<String> invocationArguments) {
        if (element == null) {
            return;
        }
        List<String> normalizedInvocationArguments = normalizeArguments(invocationArguments);
        for (JiraTestKey annotation : element.getAnnotationsByType(JiraTestKey.class)) {
            String key = normalize(annotation.value());
            if (key.isEmpty()) {
                continue;
            }
            List<String> expectedArguments = normalizeArguments(Arrays.asList(annotation.arguments()));
            boolean matches = expectedArguments.equals(normalizedInvocationArguments);
            if (matches) {
                keys.add(key);
            }
        }
    }

    private static List<String> normalizeArguments(List<String> values) {
        return values.stream().map(JiraAnnotations::normalize).toList();
    }

    private static String normalize(String raw) {
        return raw == null ? "" : raw.trim();
    }

    private static <T extends Annotation> Set<String> collectValues(
        Class<?> testClass,
        Method testMethod,
        Class<T> type,
        Function<T, String> extractor,
        boolean skipBlank
    ) {
        Set<String> values = new LinkedHashSet<>();
        addValues(values, testClass, type, extractor, skipBlank);
        addValues(values, testMethod, type, extractor, skipBlank);
        return values;
    }

    private static <T extends Annotation> void addValues(
        Set<String> values,
        AnnotatedElement element,
        Class<T> type,
        Function<T, String> extractor,
        boolean skipBlank
    ) {
        if (element == null) {
            return;
        }
        T[] annotations = element.getAnnotationsByType(type);
        for (T annotation : annotations) {
            String raw = extractor.apply(annotation);
            String normalized = normalize(raw);
            if (skipBlank && normalized.isEmpty()) {
                continue;
            }
            values.add(normalized);
        }
    }
}


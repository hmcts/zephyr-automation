package uk.hmcts.zephyr.automation.junit5;

import org.junit.jupiter.api.extension.ExtensionContext;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraComponent;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraDefect;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraIgnore;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraKey;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraLabel;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraLink;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraNfr;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public final class JiraAnnotations {

    private JiraAnnotations() {
    }

    public static JiraAnnotationMetadata fromContext(ExtensionContext context) {
        Class<?> testClass = context.getTestClass().orElse(null);
        Method testMethod = context.getTestMethod().orElse(null);

        return new JiraAnnotationMetadata(
            collectValues(testClass, testMethod, JiraKey.class, JiraKey::value, true),
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
            String normalized = raw == null ? "" : raw.trim();
            if (skipBlank && normalized.isEmpty()) {
                continue;
            }
            values.add(normalized);
        }
    }
}


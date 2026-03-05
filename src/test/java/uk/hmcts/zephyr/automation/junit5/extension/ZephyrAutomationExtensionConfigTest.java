package uk.hmcts.zephyr.automation.junit5.extension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ZephyrAutomationExtensionConfigTest {

    @Test
    void getConfig_usesConfiguredLocationAndCachesInStore() {
        TestStore store = new TestStore();
        ExtensionContext root = rootWithStore(store);
        ExtensionContext context = contextWithRoot(root);
        when(context.getConfigurationParameter("zephyr.report.location"))
            .thenReturn(Optional.of("reports/custom.json"));

        ZephyrAutomationExtensionConfig first = ZephyrAutomationExtensionConfig.getConfig(context);
        ZephyrAutomationExtensionConfig second = ZephyrAutomationExtensionConfig.getConfig(context);

        Path expected = Path.of("reports/custom.json").toAbsolutePath().normalize();
        assertEquals(expected, first.reportLocation);
        assertSame(first, second);
    }

    @Test
    void getConfig_reusesInstanceAcrossDifferentContextsSharingRoot() {
        TestStore store = new TestStore();
        ExtensionContext root = rootWithStore(store);
        ExtensionContext firstContext = contextWithRoot(root);
        ExtensionContext secondContext = contextWithRoot(root);
        when(firstContext.getConfigurationParameter("zephyr.report.location"))
            .thenReturn(Optional.of("custom/report.json"));
        when(secondContext.getConfigurationParameter("zephyr.report.location"))
            .thenReturn(Optional.of("ignored.json"));

        ZephyrAutomationExtensionConfig first = ZephyrAutomationExtensionConfig.getConfig(firstContext);
        ZephyrAutomationExtensionConfig second = ZephyrAutomationExtensionConfig.getConfig(secondContext);

        assertSame(first, second);
    }

    @Test
    void constructorWithoutParameter_usesDefaultBuildLocation() {
        ExtensionContext context = mock(ExtensionContext.class);
        when(context.getConfigurationParameter("zephyr.report.location")).thenReturn(Optional.empty());

        ZephyrAutomationExtensionConfig config = new ZephyrAutomationExtensionConfig(context);

        Path expected = Path.of("build/zephyr-report.json").toAbsolutePath().normalize();
        assertEquals(expected, config.reportLocation);
    }

    private ExtensionContext contextWithRoot(ExtensionContext root) {
        ExtensionContext context = mock(ExtensionContext.class);
        when(context.getRoot()).thenReturn(root);
        return context;
    }

    private ExtensionContext rootWithStore(TestStore store) {
        ExtensionContext root = mock(ExtensionContext.class);
        when(root.getStore(ZephyrAutomationExtension.NAMESPACE)).thenReturn(store);
        return root;
    }

    private static final class TestStore implements ExtensionContext.Store {
        private final Map<Object, Object> values = new ConcurrentHashMap<>();

        @Override
        public Object get(Object key) {
            return values.get(key);
        }

        @Override
        public <V> V get(Object key, Class<V> requiredType) {
            return requiredType.cast(values.get(key));
        }

        @Override
        public Object remove(Object key) {
            return values.remove(key);
        }

        @Override
        public <V> V remove(Object key, Class<V> valueType) {
            return valueType.cast(values.remove(key));
        }

        @Override
        public <K, V> V getOrComputeIfAbsent(K key, Function<K, V> defaultCreator, Class<V> requiredType) {
            Object value = values.computeIfAbsent(key, ignored -> defaultCreator.apply(key));
            return requiredType.cast(value);
        }

        @Override
        public <K, V> V getOrComputeIfAbsent(K key, Function<K, V> defaultCreator) {
            throw new UnsupportedOperationException("Use typed overload");
        }

        @Override
        public void put(Object key, Object value) {
            values.put(key, value);
        }
    }
}

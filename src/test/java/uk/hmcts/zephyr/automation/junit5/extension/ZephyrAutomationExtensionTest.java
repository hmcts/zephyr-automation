package uk.hmcts.zephyr.automation.junit5.extension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import uk.hmcts.zephyr.automation.junit5.extension.ZephyrAutomationExtension.Aggregator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ZephyrAutomationExtensionTest {

    private static final String AGGREGATOR_KEY = "ZEPHYR_AGGREGATOR";

    private final ZephyrAutomationExtension extension = new ZephyrAutomationExtension();

    @Test
    void testSuccessful_delegatesToAggregator() {
        Aggregator aggregator = mock(Aggregator.class);
        ExtensionContext context = contextWithMockAggregator(aggregator);

        extension.testSuccessful(context);

        verify(aggregator).addPassedTest(context);
    }

    @Test
    void testFailed_delegatesToAggregator() {
        Aggregator aggregator = mock(Aggregator.class);
        ExtensionContext context = contextWithMockAggregator(aggregator);
        Throwable cause = new IllegalStateException("boom");

        extension.testFailed(context, cause);

        verify(aggregator).addFailedTest(context, cause);
    }

    @Test
    void testAborted_delegatesToAggregator() {
        Aggregator aggregator = mock(Aggregator.class);
        ExtensionContext context = contextWithMockAggregator(aggregator);
        Throwable cause = new RuntimeException("aborted");

        extension.testAborted(context, cause);

        verify(aggregator).addAbortedTest(context, cause);
    }

    @Test
    void testDisabled_delegatesToAggregator() {
        Aggregator aggregator = mock(Aggregator.class);
        ExtensionContext context = contextWithMockAggregator(aggregator);

        extension.testDisabled(context, Optional.of("ignored"));

        verify(aggregator).addDisabledTest(context, "ignored");
    }

    private ExtensionContext contextWithMockAggregator(Aggregator aggregator) {
        InMemoryStore store = new InMemoryStore();
        store.put(AGGREGATOR_KEY, aggregator);
        return contextWithStore(store);
    }

    private ExtensionContext contextWithStore(InMemoryStore store) {
        ExtensionContext context = mock(ExtensionContext.class);
        ExtensionContext root = rootContextWithStore(store);
        when(context.getRoot()).thenReturn(root);
        return context;
    }

    private ExtensionContext rootContextWithStore(InMemoryStore store) {
        ExtensionContext root = mock(ExtensionContext.class);
        when(root.getRoot()).thenReturn(root);
        when(root.getStore(ZephyrAutomationExtension.NAMESPACE)).thenReturn(store);
        return root;
    }

    private static final class InMemoryStore implements ExtensionContext.Store {
        private final Map<Object, Object> data = new ConcurrentHashMap<>();

        public void put(Object key, Object value) {
            data.put(key, value);
        }

        @Override
        public Object get(Object key) {
            return data.get(key);
        }

        @Override
        public <V> V get(Object key, Class<V> requiredType) {
            return requiredType.cast(data.get(key));
        }

        @Override
        public Object remove(Object key) {
            return data.remove(key);
        }

        @Override
        public <V> V remove(Object key, Class<V> valueType) {
            return valueType.cast(data.remove(key));
        }

        @Override
        public <K, V> V getOrComputeIfAbsent(K key, Function<K, V> defaultCreator, Class<V> requiredType) {
            Object value = data.computeIfAbsent(key, unused -> defaultCreator.apply(key));
            return requiredType.cast(value);
        }

        @Override
        public <K, V> V getOrComputeIfAbsent(K key, Function<K, V> defaultCreator) {
            throw new UnsupportedOperationException("Use typed variant");
        }
    }
}

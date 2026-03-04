package uk.hmcts.zephyr.automation.junit5.extension;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import uk.hmcts.zephyr.automation.junit5.JiraAnnotations;
import uk.hmcts.zephyr.automation.junit5.model.Junit5ZephyrReport;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ZephyrAutomationExtension implements TestWatcher, AfterAllCallback {


    public static final ExtensionContext.Namespace NAMESPACE =
        ExtensionContext.Namespace.create(ZephyrAutomationExtension.class);


    private static Aggregator getJunit5ZephyrReport(ExtensionContext context) {
        // root store is shared across all tests in the same engine run
        ExtensionContext root = context.getRoot();
        return root.getStore(NAMESPACE)
            .getOrComputeIfAbsent(Aggregator.class, k -> new Aggregator(), Aggregator.class);
    }

    @Override
    public void testSuccessful(ExtensionContext extensionContext) {
        getJunit5ZephyrReport(extensionContext).addPassedTest(extensionContext);
    }

    @Override
    public void testFailed(ExtensionContext extensionContext, Throwable cause) {
        getJunit5ZephyrReport(extensionContext).addFailedTest(extensionContext, cause);
    }

    @Override
    public void testAborted(ExtensionContext extensionContext, Throwable cause) {
        getJunit5ZephyrReport(extensionContext).addAbortedTest(extensionContext, cause);
    }

    @Override
    public void testDisabled(ExtensionContext extensionContext, Optional<String> reason) {
        getJunit5ZephyrReport(extensionContext).addDisabledTest(extensionContext, reason.orElse(""));
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        // In parallel runs, afterAll will be invoked for multiple containers (classes).
        // We only want to flush once per root.
        getJunit5ZephyrReport(extensionContext).saveReport(extensionContext);
    }

    static final class Aggregator {
        private final Queue<Junit5ZephyrReport.Test> tests = new ConcurrentLinkedQueue<>();
        private final AtomicBoolean flushed = new AtomicBoolean(false);
        private final String runId = UUID.randomUUID().toString();
        private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();


        void add(Junit5ZephyrReport.Test test) {
            this.tests.add(test);
        }

        void saveReport(ExtensionContext extensionContext) {
            if (!flushed.compareAndSet(false, true)) {
                return;
            }
            try {
                ZephyrAutomationExtensionConfig config = ZephyrAutomationExtensionConfig.getConfig(extensionContext);
                Path reportLocation = config.reportLocation;
                Files.createDirectories(reportLocation.getParent());

                Files.writeString(reportLocation, objectMapper.writeValueAsString(generateReport()),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private Junit5ZephyrReport generateReport() {
            Junit5ZephyrReport report = new Junit5ZephyrReport();
            report.setRunId(runId);
            report.setGeneratedAt(OffsetDateTime.now());
            report.getTests().addAll(tests);
            return report;
        }

        Junit5ZephyrReport.Test base(ExtensionContext extensionContext, Junit5ZephyrReport.Test.Status status) {
            return base(extensionContext, status, null, null);
        }

        Junit5ZephyrReport.Test base(ExtensionContext extensionContext, Junit5ZephyrReport.Test.Status status,
                                     Throwable throwable) {
            Optional<Throwable> throwableOptional = Optional.ofNullable(throwable);
            return base(extensionContext, status,
                throwableOptional.map(throwable1 -> throwable1.getClass().getName()).orElse(null),
                throwableOptional.map(Throwable::getMessage).orElse(null)
            );
        }

        Junit5ZephyrReport.Test base(ExtensionContext extensionContext,
                                     Junit5ZephyrReport.Test.Status status,
                                     String errorType,
                                     String errorMessage) {
            return new Junit5ZephyrReport.Test(
                extensionContext.getUniqueId(),
                extensionContext.getDisplayName(),
                extensionContext.getRequiredTestClass().getName(),
                extensionContext.getTestMethod().map(Method::getName).orElse(""),
                status,
                errorType,
                errorMessage,
                new ArrayList<>(extensionContext.getTags()),
                JiraAnnotations.fromContext(extensionContext)
            );
        }

        public void addPassedTest(ExtensionContext extensionContext) {
            this.add(base(extensionContext, Junit5ZephyrReport.Test.Status.PASSED));
        }

        public void addFailedTest(ExtensionContext extensionContext, Throwable cause) {
            this.add(base(extensionContext, Junit5ZephyrReport.Test.Status.FAILED, cause));

        }

        public void addAbortedTest(ExtensionContext extensionContext, Throwable cause) {
            this.add(base(extensionContext, Junit5ZephyrReport.Test.Status.ABORTED, cause));
        }

        public void addDisabledTest(ExtensionContext extensionContext, String reason) {
            this.add(base(extensionContext, Junit5ZephyrReport.Test.Status.DISABLED, null, reason));
        }
    }
}
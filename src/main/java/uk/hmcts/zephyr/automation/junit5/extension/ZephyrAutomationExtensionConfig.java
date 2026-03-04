package uk.hmcts.zephyr.automation.junit5.extension;

import org.junit.jupiter.api.extension.ExtensionContext;

import java.nio.file.Path;

public class ZephyrAutomationExtensionConfig {
    final Path reportLocation;

    public static ZephyrAutomationExtensionConfig getConfig(ExtensionContext context) {
        return context.getRoot()
            .getStore(ZephyrAutomationExtension.NAMESPACE)
            .getOrComputeIfAbsent(ZephyrAutomationExtensionConfig.class,
                key -> new ZephyrAutomationExtensionConfig(context),
                ZephyrAutomationExtensionConfig.class);
    }


    ZephyrAutomationExtensionConfig(ExtensionContext context) {
        String location = context
            .getConfigurationParameter("zephyr.report.location")
            .orElse(defaultLocation());

        this.reportLocation = Path.of(location).toAbsolutePath().normalize();
    }

    private String defaultLocation() {
        // Sensible default if nothing is configured
        return "build/zephyr-report.json";
    }
}

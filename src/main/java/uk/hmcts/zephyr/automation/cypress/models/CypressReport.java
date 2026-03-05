package uk.hmcts.zephyr.automation.cypress.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.actions.ZephyrTest;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;

import java.util.ArrayList;
import java.util.List;

@Data
public class CypressReport {
    private Meta meta;
    private Stats stats;
    private List<CypressTest> tests;


    @Data
    public static class Meta {
        private String reporter;
        private String generatedAt;
        private String startedAt;
        private String endedAt;
    }

    @Data
    public static class Stats {
        private Integer tests;
        private Integer passes;
        private Integer failures;
        private Integer pending;
        private Integer durationMs;
    }

    @Data
    public static class CypressTest implements ZephyrTest {
        private String title;
        private String fullTitle;
        private String file;
        private List<String> parents;
        private Integer durationMs;
        private String status;
        private List<String> tags;
        private ConfigOverrides configOverrides;

        public List<String> getTags() {
            if (tags == null) {
                tags = new ArrayList<>();
            }
            return tags;
        }

        public void addTag(String tag) {
            if (!hasTag(tag)) {
                getTags().add(tag);
            }
        }

        public boolean hasTag(String tagName) {
            return getTags().stream().anyMatch(tag -> tag.equals(tagName));
        }


        @Override
        @JsonIgnore
        public String getName() {
            return this.title;
        }

        @Override
        @JsonIgnore
        public String getLocationDisplayName() {
            if (parents == null || parents.isEmpty()) {
                throw new RuntimeException("No parents");
            }
            return String.join(" > ", parents);
        }

        @Override
        @JsonIgnore
        public String getGitHubLink() {
            return Config.getGithubRepoBaseSrcDir() + "/" + file;
        }

        @Override
        @JsonIgnore
        public String getNameAndLocation() {
            return getName() + " (" + getLocationDisplayName() + ")";
        }

        @Override
        @JsonIgnore
        public ZephyrConstants.ExecutionStatus getZephyrExecutionStatus() {
            //If all steps passed, mark as pass.
            return switch (status) {
                case "passed" -> ZephyrConstants.ExecutionStatus.PASS;
                case "failed" -> ZephyrConstants.ExecutionStatus.FAIL;
                case "pending", "skipped" -> ZephyrConstants.ExecutionStatus.UNEXECUTED;
                default ->
                    //Default to unexecuted if there are no steps or all steps are skipped or undefined
                    ZephyrConstants.ExecutionStatus.UNEXECUTED;
            };
        }
    }

    @Data
    public static class ConfigOverrides {
        private List<TestConfig> testConfigList;
        private UnverifiedTestConfig unverifiedTestConfig;
        private String applied;
    }

    @Data
    public static class TestConfig {
        private String overrideLevel;
        private Overrides overrides;
        private InvocationDetails invocationDetails;
    }

    @Data
    public static class Overrides {
        private List<String> tags;
    }

    @Data
    public static class InvocationDetails {
        private String function;
        private String fileUrl;
        private String originalFile;
        private Integer line;
        private Integer column;
        private String whitespace;
        private String stack;
    }

    @Data
    public static class UnverifiedTestConfig {
        private List<String> tags;
    }

}

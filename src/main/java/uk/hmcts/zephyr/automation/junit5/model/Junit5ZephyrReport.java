package uk.hmcts.zephyr.automation.junit5.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.actions.ZephyrTest;
import uk.hmcts.zephyr.automation.junit5.JiraAnnotationMetadata;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Junit5ZephyrReport {
    private String runId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime generatedAt;
    private final List<Test> tests = new ArrayList<>();

    @Data
    @AllArgsConstructor
    public static class Test implements ZephyrTest {
        private String id;
        private String displayName;
        private String className;
        private String methodName;
        private Status status;
        private String errorType;
        private String errorMessage;
        private List<String> tags;
        private JiraAnnotationMetadata metadata;

        @Override
        @JsonIgnore
        public String getName() {
            return this.getDisplayName();
        }

        @Override
        @JsonIgnore
        public String getGitHubLink() {
            return Config.getGithubRepoBaseSrcDir() + className.replace(".", "/")
                .replaceAll("\\$.*", ".java");
        }

        @Override
        @JsonIgnore
        public String getNameAndLocation() {
            return getName() + " (" + this.getClassName() + ")";
        }

        @Override
        @JsonIgnore
        public ZephyrConstants.ExecutionStatus getZephyrExecutionStatus() {
            return switch (status) {
                case PASSED -> ZephyrConstants.ExecutionStatus.PASS;
                case FAILED, ABORTED -> ZephyrConstants.ExecutionStatus.FAIL;
                case DISABLED -> ZephyrConstants.ExecutionStatus.UNEXECUTED;
            };
        }

        @Override
        @JsonIgnore
        public String getLocationDisplayName() {
            return this.getClassName();
        }

        public enum Status {
            PASSED,
            FAILED,
            ABORTED,
            DISABLED
        }
    }
}

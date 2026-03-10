package uk.hmcts.zephyr.automation.cucumber.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.actions.Attachment;
import uk.hmcts.zephyr.automation.actions.ZephyrTest;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CucumberFeature {
    private int line;
    private List<Element> elements;
    private String name;
    private String description;
    private String id;
    private String keyword;
    private String uri;
    private List<Tag> tags;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Element implements ZephyrTest {

        @JsonProperty("start_timestamp")
        private String startTimestamp;
        private List<Hook> before;
        private int line;
        private String name;
        private String description;
        private String id;
        private List<Hook> after;
        private String type;
        private String keyword;
        private List<Step> steps;
        private List<Tag> tags;

        @JsonIgnore
        private CucumberFeature cucumberFeature;

        public List<Tag> getTags() {
            if (tags == null) {
                tags = new ArrayList<>();
            }
            return tags;
        }

        public void addTag(Tag tag) {
            getTags().add(tag);
        }

        public boolean hasTag(String tagName) {
            return getTags().stream().anyMatch(tag -> tag.getName().equals(tagName));
        }

        @Override
        @JsonIgnore
        public String getGitHubLink() {
            return Config.getGithubRepoBaseSrcDir() + "/" +
                cucumberFeature.getUri().replace("classpath:", "") + "#L" + getLine();
        }

        @Override
        @JsonIgnore
        public String getLocationDisplayName() {
            return getCucumberFeature().getName();
        }

        @Override
        @JsonIgnore
        public String getNameAndLocation() {
            return getName() + " (line " + getLine() + " in feature " + cucumberFeature.getUri() + ")";
        }

        @Override
        @JsonIgnore
        public ZephyrConstants.ExecutionStatus getZephyrExecutionStatus() {
            if (getSteps().stream()
                .map(step -> step.getResult().getStatus())
                .allMatch(s -> s.equalsIgnoreCase("passed"))) {
                return ZephyrConstants.ExecutionStatus.PASS;
            }
            if (getSteps().stream()
                .map(step -> step.getResult().getStatus())
                .anyMatch(s -> s.equalsIgnoreCase("failed"))) {
                return ZephyrConstants.ExecutionStatus.FAIL;
            }
            return ZephyrConstants.ExecutionStatus.UNEXECUTED;
        }

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Step {
            private Result result;
            private int line;
            private String name;
            private Match match;
            private String keyword;
            private List<Row> rows;
            private List<Embedding> embeddings;

            @Getter
            @Setter
            @AllArgsConstructor
            @NoArgsConstructor
            public static class Row {
                private List<String> cells;
            }

            @Getter
            @Setter
            @AllArgsConstructor
            @NoArgsConstructor
            public static class Embedding implements Attachment {
                @JsonProperty("mime_type")
                private String mimeType;
                private String data;


                @Override
                public byte[] getContent() {
                    return Base64.getDecoder().decode(data);
                }

                @Override
                public String getFileName() {
                    String fileExtension = mimeType.split("/")[1];
                    return "embedding." + fileExtension;
                }

                @Override
                public String getContentType() {
                    return mimeType;
                }
            }
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Tag {
        private String name;
        private String type;
        private Location location;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Location {
        private int line;
        private int column;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Hook {
        private Result result;
        private Match match;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Result {
        private long duration;
        private String status;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Match {
        private List<MatchArgument> arguments;
        private String location;


        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        public static class MatchArgument {

            private String val;
            private int offset;
        }
    }


}

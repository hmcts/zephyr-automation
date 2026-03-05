package uk.hmcts.zephyr.automation.jira.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.hmcts.zephyr.automation.jira.JiraConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
public class JiraIssueFieldsWrapper {
    private Fields fields;

    @Data
    @SuperBuilder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Fields {
        private Project project;
        private String summary;
        private IssueType issuetype;
        private List<Component> components;
        private List<String> labels;
        private Reporter reporter;
        private String description;

        @JsonIgnore
        private String epicLink;

        @JsonAnyGetter
        public Map<String, Object> getDynamicFields() {
            Map<String, Object> dynamic = new HashMap<>();
            if (epicLink != null) {
                dynamic.put(JiraConfig.getEpicLinkCustomFieldId(), epicLink);
            }
            return dynamic;
        }

        public void setSummary(String summary) {
            this.summary = sanitizeSummary(summary);
        }

        private static String sanitizeSummary(String summary) {
            if (summary == null) {
                return null;
            }
            String withoutNewLines = summary.replace('\r', ' ').replace('\n', ' ');
            return withoutNewLines.length() > 255
                ? withoutNewLines.substring(0, 255)
                : withoutNewLines;
        }
    }

    @Data
    @SuperBuilder
    @AllArgsConstructor
    public static class Project {
        private String id;
    }

    @Data
    @SuperBuilder
    @AllArgsConstructor
    public static class IssueType {
        private String id;
    }

    @Data
    @SuperBuilder
    @AllArgsConstructor
    public static class Component {
        private String id;
    }

    @Data
    @SuperBuilder
    @AllArgsConstructor
    public static class Reporter {
        private String name;
    }
}

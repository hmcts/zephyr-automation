package uk.hmcts.zephyr.automation.jira.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

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
        private Parent parent;
        private JiraDescription description;

        @JsonIgnore
        private Map<String, Object> dynamicFields;


        @JsonAnyGetter
        public Map<String, Object> getDynamicFields() {
            if (dynamicFields == null) {
                dynamicFields = new HashMap<>();
            }
            return dynamicFields;
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

        public void addDynamicField(String fieldId, Object value) {
            getDynamicFields().put(fieldId, value);
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
    public static class Parent {
        private String key;
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
        private String id;
    }
}

package uk.hmcts.zephyr.automation.jira.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    }

    @Data
    @SuperBuilder
    public static class Project {
        private String id;
    }

    @Data
    @SuperBuilder
    public static class IssueType {
        private String id;
    }

    @Data
    @SuperBuilder
    public static class Component {
        private String id;
    }

    @Data
    @SuperBuilder
    public static class Reporter {
        private String name;
    }
}

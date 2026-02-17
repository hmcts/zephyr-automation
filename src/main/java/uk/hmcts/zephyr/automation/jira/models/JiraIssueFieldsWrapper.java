package uk.hmcts.zephyr.automation.jira.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.hmcts.zephyr.automation.jira.JiraConstants;

import java.util.List;

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
        @JsonProperty(JiraConstants.EPIC_LINK_CUSTOM_FIELD)
        private String epicLink;
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

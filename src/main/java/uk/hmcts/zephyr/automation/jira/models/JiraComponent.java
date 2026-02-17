package uk.hmcts.zephyr.automation.jira.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraComponent {
    private String self;
    private String id;
    private String name;
    private String description;
    private String assigneeType;
    private Assignee assignee;
    private String realAssigneeType;
    private Assignee realAssignee;

    @JsonProperty("isAssigneeTypeValid")
    private Boolean assigneeTypeValid;

    private String project;
    private Integer projectId;
    private Boolean archived;
    private Boolean deleted;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Assignee {
        private String self;
        private String key;
        private String name;
        private AvatarUrls avatarUrls;
        private String displayName;
        private Boolean active;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AvatarUrls {
        @JsonProperty("48x48")
        private String x48x48;

        @JsonProperty("24x24")
        private String x24x24;

        @JsonProperty("16x16")
        private String x16x16;

        @JsonProperty("32x32")
        private String x32x32;
    }
}

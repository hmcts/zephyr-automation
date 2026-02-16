package uk.hmcts.zephyr.automation.cucumber.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Element {

    private String start_timestamp;
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

    public void addTag(Tag jiraTag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        tags.add(jiraTag);
    }

    public boolean hasTag(String tagName) {
        if (tags == null) {
            return false;
        }
        return tags.stream().anyMatch(tag -> tag.getName().equals(tagName));
    }
}

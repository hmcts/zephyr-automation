package uk.hmcts.zephyr.automation.cucumber.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.actions.ZephyrTest;
import uk.hmcts.zephyr.automation.zephyr.ZephyrConstants;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Element implements ZephyrTest {

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
    private Feature feature;

    public List<Tag> getTags() {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        return tags;
    }

    public void addTag(Tag jiraTag) {
        getTags().add(jiraTag);
    }

    public boolean hasTag(String tagName) {
        return getTags().stream().anyMatch(tag -> tag.getName().equals(tagName));
    }


    @Override
    public String getGitHubLink() {
        return feature.getUri().replace("classpath:",
            Config.githubRepoBaseSrcDir + "/resources/") + "#L" + getLine();
    }

    @Override
    public String getLocationDisplayName() {
        return getFeature().getName();
    }

    @Override
    public String getNameAndLocation() {
        return getName() + " (line " + getLine() + " in feature " + feature.getName() + ")";
    }

    @Override
    public ZephyrConstants.ExecutionStatus getZephyrExecutionStatus() {
        //If all steps passed, mark as pass.
        if (getSteps().stream()
            .map(step -> step.getResult().getStatus())
            .allMatch(s -> s.equalsIgnoreCase("passed"))) {
            return ZephyrConstants.ExecutionStatus.PASS;
        }
        //If any step failed, mark as fail.
        if (getSteps().stream()
            .map(step -> step.getResult().getStatus())
            .anyMatch(s -> s.equalsIgnoreCase("failed"))) {
            return ZephyrConstants.ExecutionStatus.FAIL;
        }
        //Default to unexecuted if there are no steps or all steps are skipped or undefined
        return ZephyrConstants.ExecutionStatus.UNEXECUTED;
    }
}

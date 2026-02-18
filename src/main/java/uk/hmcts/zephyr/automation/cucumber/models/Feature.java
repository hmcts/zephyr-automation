package uk.hmcts.zephyr.automation.cucumber.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Feature {
    private int line;
    private List<Element> elements;
    private String name;
    private String description;
    private String id;
    private String keyword;
    private String uri;
    private List<Tag> tags;
}

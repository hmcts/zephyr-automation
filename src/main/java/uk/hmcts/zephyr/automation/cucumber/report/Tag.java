package uk.hmcts.zephyr.automation.cucumber.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tag {
    private String name;
    private String type; // optional, may be null
    private Location location; // optional, may be null
}

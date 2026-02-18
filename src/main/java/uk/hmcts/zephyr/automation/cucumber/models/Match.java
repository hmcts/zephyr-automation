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
public class Match {
    private List<MatchArgument> arguments;
    private String location;
}

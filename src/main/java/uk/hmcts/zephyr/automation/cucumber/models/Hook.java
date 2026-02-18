package uk.hmcts.zephyr.automation.cucumber.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Hook {
    private Result result;
    private Match match;
}

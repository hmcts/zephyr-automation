package uk.hmcts.zephyr.automation.cucumber.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Step {
    private Result result;
    private int line;
    private String name;
    private Match match;
    private String keyword;
    private List<Row> rows;
}

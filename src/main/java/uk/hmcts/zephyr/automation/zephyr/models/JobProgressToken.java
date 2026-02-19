package uk.hmcts.zephyr.automation.zephyr.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobProgressToken {
    private String jobProgressToken;
}


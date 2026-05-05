package uk.hmcts.zephyr.automation.jira.models;

import lombok.Getter;

@Getter
public enum LinkType {
    RELATES("Relates"),
    CONTRIBUTES("Contributes");


    private final String type;

    LinkType(String type) {
        this.type = type;
    }
}

package uk.hmcts.zephyr.automation;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZephyrAutomation {

    public static void main(String[] args) {
        Config.instantiate(args);
        Config.getProcessType().processAction(Config.getActionType());
    }
}
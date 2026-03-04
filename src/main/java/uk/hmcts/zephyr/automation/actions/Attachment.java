package uk.hmcts.zephyr.automation.actions;

public interface Attachment {

    String getFileName();

    String getContentType();

    byte[] getContent();
}

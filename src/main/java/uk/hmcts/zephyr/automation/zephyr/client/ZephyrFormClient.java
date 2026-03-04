package uk.hmcts.zephyr.automation.zephyr.client;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.form.FormData;

public interface ZephyrFormClient {

    @RequestLine("POST /attachment?entityType={entityType}&entityId={entityId}")
    @Headers("Content-Type: multipart/form-data")
    void attachEvidence(
        @Param("entityType") String entityType,
        @Param("entityId") Long entityId,
        @Param("file") FormData formData
    );
}

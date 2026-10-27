package uk.hmcts.zephyr.automation.zephyr.client;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.form.FormData;

public interface ZephyrFormClient {

    @RequestLine("POST /public/rest/api/1.0/attachment?entityType={entityType}&entityId={entityId}")
    @Headers("Content-Type: multipart/form-data")
    void attachEvidence(
        @Param("entityType") String entityType,
        @Param("entityId") String entityId,
        @Param("file") FormData formData
    );
}

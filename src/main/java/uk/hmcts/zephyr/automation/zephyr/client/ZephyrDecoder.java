package uk.hmcts.zephyr.automation.zephyr.client;

import feign.Response;
import feign.codec.Decoder;
import feign.codec.StringDecoder;

import java.io.IOException;
import java.lang.reflect.Type;

public class ZephyrDecoder implements Decoder {
    private final Decoder stringDecoder = new StringDecoder();
    private final Decoder jsonDecoder;

    public ZephyrDecoder(Decoder jsonDecoder) {
        this.jsonDecoder = jsonDecoder;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException {
        if (String.class.equals(type)) {
            return stringDecoder.decode(response, type);
        }
        return jsonDecoder.decode(response, type);
    }
}

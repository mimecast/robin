package com.mimecast.robin.util;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapUtilsTest {

    @Test
    @SuppressWarnings("unchecked")
    void flattenMapValid() {
        List<String> data = new ArrayList<>();

        MapUtils.flattenMap(
                new Gson().fromJson("{\"meta\":{\"status\":200},\"data\":[{\"valid\":true}],\"errors\":[]}", Map.class),
                "",
                data
        );

        assertEquals(3, data.size());
        assertEquals("meta>status: 200.0", data.get(0));
        assertEquals("data>0>valid: true", data.get(1));
        assertEquals("errors: []", data.get(2));
    }

    @Test
    @SuppressWarnings("unchecked")
    void flattenMapError() {
        List<String> data = new ArrayList<>();

        MapUtils.flattenMap(
                new Gson().fromJson("{\"meta\":{\"status\":200},\"data\":[{\"valid\":false}],\"errors\":[{\"key\":\"EndpointException\",\"errors\":[{\"code\":\"502\",\"message\":\"Unable to process request!\",\"retryable\":false}]}]}", Map.class),
                "",
                data
        );// {"key":"EndpointException","errors":[{"code":"502","message":"We did not get any response from the termite server after trying multiple times!","retryable":false}]}

        assertEquals(6, data.size());
        assertEquals("meta>status: 200.0", data.get(0));
        assertEquals("data>0>valid: false", data.get(1));
        assertEquals("errors>0>key: EndpointException", data.get(2));
        assertEquals("errors>0>errors>0>code: 502", data.get(3));
        assertEquals("errors>0>errors>0>message: Unable to process request!", data.get(4));
        assertEquals("errors>0>errors>0>retryable: false", data.get(5));
    }
}

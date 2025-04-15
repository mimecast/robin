package com.mimecast.robin.http;

import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.main.Factories;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpClientTest {
    static HttpRequest httpRequest;
    static Request request;
    Map<String, Object> configMap = new HashMap<>();

    BasicConfig config = new BasicConfig(configMap);

    @BeforeAll
    static void setup() {
        httpRequest = new HttpRequest("http://example.com");
        request = new Request.Builder()
                .url(httpRequest.getUrl())
                .method(httpRequest.getMethod().toString(), null)
                .build();
    }

    @Test
    void isSuccessful() throws IOException, NoSuchAlgorithmException, KeyManagementException {

        Response response = new Response.Builder()
                .request(request)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(200)
                .header("dummy-header", "dummy-value")
                .message("OK")
                .body(ResponseBody.create("OK", okhttp3.MediaType.parse("text/plain")))
                .build();

        MockHttpClient mockHttpClient = new MockHttpClient(config, Factories.getTrustManager(), response);

        HttpResponse httpResponse = mockHttpClient.execute(httpRequest);

        assertTrue(response.isSuccessful());
        assertEquals("dummy-value", response.header("dummy-header"));
        assertEquals("OK", httpResponse.getBody());
    }

    @Test
    void isUnsuccessful() throws IOException, NoSuchAlgorithmException, KeyManagementException {

        Response response = new Response.Builder()
                .request(request)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(400).message("OK")
                .body(ResponseBody.create("cheep cheep", okhttp3.MediaType.parse("text/plain")))
                .build();

        MockHttpClient mockHttpClient = new MockHttpClient(config, Factories.getTrustManager(), response);

        HttpResponse httpResponse = mockHttpClient.execute(httpRequest);

        assertFalse(response.isSuccessful());
        assertEquals("cheep cheep", httpResponse.getBody());
    }
}
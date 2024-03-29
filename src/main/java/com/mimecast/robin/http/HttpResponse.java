package com.mimecast.robin.http;

import java.util.Map;
import java.util.TreeMap;

/**
 * HTTP/S response container.
 */
public class HttpResponse {

    /**
     * Success container.
     */
    private boolean success = false;

    /**
     * Is successfull.
     *
     * @return Boolean.
     */
    public boolean isSuccessfull() {
        return success;
    }

    /**
     * Sets success.
     *
     * @param success Boolean.
     * @return Self.
     */
    HttpResponse setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    /**
     * Headers container.
     */
    private final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * Gets HTTP/S response headers.
     *
     * @return Map of String, String.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Gets HTTP/S response header by name.
     *
     * @param name Header name.
     * @return Self.
     */
    String getHeader(String name) {
        return headers.get(name);
    }

    /**
     * Adds HTTP/S response header.
     *
     * @param name  Header name.
     * @param value Header value.
     * @return Self.
     */
    HttpResponse addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Body container.
     */
    private String body;

    /**
     * Gets HTTP/S response body.
     *
     * @return String.
     */
    public String getBody() {
        return body;
    }

    /**
     * Adds HTTP/S response header.
     *
     * @param body String.
     * @return Self.
     */
    HttpResponse addBody(String body) {
        this.body = body;
        return this;
    }
}

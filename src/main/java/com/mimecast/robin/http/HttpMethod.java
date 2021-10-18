package com.mimecast.robin.http;

/**
 * HTTP/S method.
 */
public enum HttpMethod {

    /**
     * GET method.
     */
    GET("GET"),

    /**
     * POST method.
     */
    POST("POST");

    /**
     * Method.
     */
    private final String method;

    /**
     * @param method String.
     */
    HttpMethod(final String method) {
        this.method = method;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return method;
    }
}

package com.mimecast.robin.http;

/**
 * HTTP/S method.
 */
public enum HttpMethod {

    /**
     * DELETE method.
     */
    DELETE("DELETE"),

    /**
     * POST method.
     */
    POST("POST"),

    /**
     * PUT method.
     */
    PUT("PUT"),

    /**
     * GET method.
     */
    GET("GET");

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

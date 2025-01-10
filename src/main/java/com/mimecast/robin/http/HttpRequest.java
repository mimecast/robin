package com.mimecast.robin.http;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * HTTP/S request container.
 */
public class HttpRequest {

    /**
     * Request URL.
     */
    private final String url;

    /**
     * Request method.
     * <p>Default: GET
     */
    private HttpMethod method = HttpMethod.GET;

    /**
     * Headers container.
     */
    private final Map<String, String> headers = new HashMap<>();

    /**
     * Parameters container.
     */
    private final Map<String, String> params = new HashMap<>();

    /**
     * Files container.
     */
    private final Map<String, Pair<String, String>> files = new HashMap<>();

    /**
     * Content container.
     */
    private Pair<String, String> content;

    /**
     * Object container.
     */
    private Pair<byte[], String> object;

    /**
     * Constructs a new HttpRequest instance with given URL and request method.
     *
     * @param url    Request URL.
     * @param method Request method.
     */
    public HttpRequest(String url, HttpMethod method) {
        this(url);
        this.method = method;
    }

    /**
     * Constructs a new HttpRequest instance with given URL.
     *
     * @param url Request URL.
     */
    public HttpRequest(String url) {
        this.url = url;
    }

    /**
     * Gets HTTP/S request URL.
     *
     * @return String.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets HTTP/S request method.
     *
     * @return Instance of HttpMethod.
     */
    public HttpMethod getMethod() {
        return method;
    }

    /**
     * Gets HTTP/S request header.
     *
     * @return Map of String, String.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Adds HTTP/S request header.
     *
     * @param name  Header name.
     * @param value Header value.
     * @return Self.
     */
    public HttpRequest addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Gets HTTP/S POST parameters.
     *
     * @return Map of String, String.
     */
    public Map<String, String> getParams() {
        return params;
    }

    /**
     * Adds HTTP/S POST parameter.
     *
     * @param name  Param name.
     * @param value Param value.
     * @return Self.
     */
    public HttpRequest addParam(String name, String value) {
        params.put(name, value);
        return this;
    }

    /**
     * Gets HTTP/S POST files.
     *
     * @return Map of String, Pair of String, String.
     */
    public Map<String, Pair<String, String>> getFiles() {
        return files;
    }

    /**
     * Adds HTTP/S POST file.
     *
     * @param param    File param name.
     * @param file     File path.
     * @param mimeType File MIME mimeType.
     * @return Self.
     */
    public HttpRequest addFile(String param, String file, String mimeType) {
        files.put(param, new ImmutablePair<>(file, mimeType));
        return this;
    }

    /**
     * Gets HTTP/S POST content.
     *
     * @return Pair of String, String.
     */
    public Pair<String, String> getContent() {
        return content;
    }

    /**
     * Adds HTTP/S POST content.
     * <p>If set POST will NOT send params and files if any.
     *
     * @param content Content string.
     * @param type    Content MIME type.
     * @return Self.
     */
    public HttpRequest addContent(String content, String type) {
        this.content = new ImmutablePair<>(content, type);
        return this;
    }

    /**
     * Gets HTTP/S POST object.
     *
     * @return Pair of byte[], String.
     */
    public Pair<byte[], String> getObject() {
        return object;
    }

    /**
     * Adds HTTP/S POST object.
     * <p>If set POST will NOT send params and files if any.
     *
     * @param bytes   Content bytes.
     * @param type    Content MIME type.
     * @return Self.
     */
    public HttpRequest addObject(byte[] bytes, String type) {
        this.object = new ImmutablePair<>(bytes, type);
        return this;
    }

    /**
     * Returns a string representation of the contents.
     *
     * @return Header string.
     */
    @Override
    public String toString() {
        Map<String, String> safeHeaders = headers.entrySet()
                .stream().filter(entry -> !entry.getKey().equals("Authorization"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return Collections.singletonList(Stream.of(
                new AbstractMap.SimpleEntry<>("url", url),
                new AbstractMap.SimpleEntry<>("headers", safeHeaders),
                new AbstractMap.SimpleEntry<>("params", params),
                new AbstractMap.SimpleEntry<>("files", files)
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))) + "\r\n" + content;
    }
}

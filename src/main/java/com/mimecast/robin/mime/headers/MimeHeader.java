package com.mimecast.robin.mime.headers;

/**
 * MIME header container.
 */
public class MimeHeader {

    /**
     * Header name.
     */
    protected final String name;

    /**
     * Header value.
     */
    protected final String value;

    /**
     * Constructs a new MimeHeader instance with given header string.
     *
     * @param header Complete header.
     */
    public MimeHeader(String header) {
        String[] splits = header.trim().split(":", 2);
        this.name = splits.length > 0 ? splits[0].trim() : "x-unknown";
        this.value = splits.length > 1 ? splits[1].trim() : "";
    }

    /**
     * Constructs a new MimeHeader instance with given name and value.
     *
     * @param name  Header name.
     * @param value Header value.
     */
    public MimeHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Gets header name.
     *
     * @return Header name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets header value.
     *
     * @return Header value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns a string representation of the header by
     * combinging name and value separated by collon and space.
     *
     * @return Header string.
     */
    @Override
    public String toString() {
        return name + ": " + value + "\r\n";
    }
}

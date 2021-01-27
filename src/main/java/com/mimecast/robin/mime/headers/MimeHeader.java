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

package com.mimecast.robin.mime.headers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Mime headers container.
 */
public class MimeHeaders {

    /**
     * Headers list.
     */
    private final List<MimeHeader> headers = new ArrayList<>();

    /**
     * Puts header.
     *
     * @param header MimeHeader instance.
     * @return Self.
     */
    public MimeHeaders put(MimeHeader header) {
        headers.add(header);
        return this;
    }

    /**
     * Gets headers as a list.
     *
     * @return List of MimeHeader.
     */
    public List<MimeHeader> get() {
        return headers;
    }

    /**
     * Gets header by name.
     *
     * @param name Header name.
     * @return Optional of MimeHeader.
     */
    public Optional<MimeHeader> get(String name) {
        for (MimeHeader header : headers) {
            if (header.getName().equalsIgnoreCase(name)) {
                return Optional.of(header);
            }
        }
        return Optional.empty();
    }
}

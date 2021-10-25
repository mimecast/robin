package com.mimecast.robin.mime.headers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * Removed header.
     *
     * @param header MimeHeader instance.
     * @return Self.
     */
    public MimeHeaders remove(MimeHeader header) {
        headers.remove(header);
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

    /**
     * Gets header by starts with partial name.
     *
     * @param name Header name.
     * @return List of MimeHeader.
     */
    public List<MimeHeader> startsWith(String name) {
        return headers.stream()
                .filter(h -> h.getName().toLowerCase().startsWith(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Gets headers list size.
     *
     * @return Integer.
     */
    public int size() {
        return headers.size();
    }
}

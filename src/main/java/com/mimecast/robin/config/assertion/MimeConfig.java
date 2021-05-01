package com.mimecast.robin.config.assertion;

import com.mimecast.robin.assertion.client.ExternalClient;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.config.ConfigFoundation;
import com.mimecast.robin.main.Factories;
import com.mimecast.robin.mime.headers.MimeHeader;
import com.mimecast.robin.mime.parts.FileMimePart;
import com.mimecast.robin.mime.parts.MimePart;
import com.mimecast.robin.mime.parts.TextMimePart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Assertions container.
 *
 * <p>SMTP assertions may be present at both session and envelope level.
 * <br>They are done directly over the SMTP transactions.
 *
 * <p>External assertions require a client for fetching the logs.
 *
 * @see ExternalClient
 * @see Factories
 * @see BasicConfig
 */
@SuppressWarnings("unchecked")
public class MimeConfig extends ConfigFoundation {

    /**
     * MIME headers.
     */
    protected final List<MimeHeader> headers = new ArrayList<>();

    /**
     * MIME parts.
     */
    protected final List<MimePart> parts = new ArrayList<>();

    /**
     * Constructs a new AssertConfig instance.
     */
    public MimeConfig() {
        super();
    }

    /**
     * Constructs a new AssertConfig instance with given configuration map.
     *
     * @param map Configuration map.
     */
    @SuppressWarnings("rawtypes")
    public MimeConfig(Map map) {
        super(map);
    }

    /**
     * Gets SMTP assertion list.
     *
     * @return List in list.
     */
    public List<List<String>> getSmtp() {
        return getListProperty("smtp");
    }

    /**
     * Gets external assertion configuration instance.
     *
     * @return List of BasicConfig instance.
     */
    public List<BasicConfig> getExternal() {
        List<BasicConfig> external = new ArrayList<>();
        for (Object map : getListProperty("external")) {
            if (map instanceof Map) {
                external.add(new BasicConfig((Map) map));
            }
        }

        return external;
    }

    /**
     * Gets list of headers.
     *
     * @return List of MimeHeader.
     */
    public List<MimeHeader> getHeaders() {
        getHeaders(headers, getListProperty("headers"));

        return headers;
    }

    /**
     * Gets headers as MimeHeader instances.
     *
     * @param container List of MimeHeader.
     * @param headers   List of headers.
     */
    public void getHeaders(List<MimeHeader> container, List headers) {
        if (container.isEmpty()) {
            for (Object header : headers) {
                if (header instanceof List && ((List<?>) header).size() > 1) {
                    container.add(new MimeHeader(((List<String>) header).get(0), ((List<String>) header).get(1)));
                }
            }
        }
    }

    /**
     * Gets list of parts.
     *
     * @return List of MimePart.
     */
    public List<MimePart> getParts() {
        if (parts.isEmpty()) {
            for (Object part : getListProperty("parts")) {
                if (part instanceof Map && ((Map<?, ?>) part).size() > 0) {
                    BasicConfig config = new BasicConfig((Map) part);

                    // Get both file and message variables.
                    String message = config.getStringProperty("message");
                    String file = config.getStringProperty("file");

                    // Choose file first if no message.
                    MimePart mimePart = null;
                    if (message == null && file != null) {
                        try {
                            mimePart = new FileMimePart(file);
                        } catch (IOException e) {
                            log.error("Unable to read part file: {}", file);
                        }
                    }

                    // Fallback to message or dummy else if file errors.
                    if (mimePart == null && message != null) {
                        mimePart = new TextMimePart(message.getBytes());
                    }

                    // Add headers to part.
                    assert mimePart != null;
                    getHeaders(mimePart.getHeaders().get(), config.getListProperty("headers"));

                    parts.add(mimePart);
                }
            }
        }

        return parts;
    }
}

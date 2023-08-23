package com.mimecast.robin.config.assertion;

import com.mimecast.robin.assertion.client.ExternalClient;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.config.ConfigFoundation;
import com.mimecast.robin.main.Factories;
import com.mimecast.robin.mime.headers.MimeHeader;
import com.mimecast.robin.mime.parts.FileMimePart;
import com.mimecast.robin.mime.parts.MimePart;
import com.mimecast.robin.mime.parts.PdfMimePart;
import com.mimecast.robin.mime.parts.TextMimePart;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.session.Session;

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
     * Constructs a new AssertConfig instance with given configuration map.
     *
     * @param map Configuration map.
     */
    @SuppressWarnings("rawtypes")
    public MimeConfig(Map map) {
        super(map);
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
    @SuppressWarnings("rawtypes")
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
        return getParts(null, null);
    }

    /**
     * Gets list of parts with magic.
     *
     * @param session  Session instance.
     * @param envelope MessageEnvelope instance.
     * @return List of MimePart.
     */
    @SuppressWarnings("rawtypes")
    public List<MimePart> getParts(Session session, MessageEnvelope envelope) {
        if (parts.isEmpty()) {
            for (Object part : getListProperty("parts")) {
                if (part instanceof Map && ((Map<?, ?>) part).size() > 0) {
                    BasicConfig config = new BasicConfig((Map) part);

                    // Get both file and message variables.
                    String message = config.getStringProperty("message");
                    String file = config.getStringProperty("file");
                    Map<String, String> pdf = config.getMapProperty("pdf");

                    MimePart mimePart = null;

                    // Make a pdf part if defined.
                    if (pdf != null && !pdf.isEmpty()) {
                        MimeConfig pdfConfig = new MimeConfig(pdf);

                        // Magic.
                        if (session != null && pdfConfig.hasProperty("text")) {
                            pdfConfig.getMap().put("text", session.magicReplace(pdfConfig.getStringProperty("text")));
                        }

                        try {
                            mimePart = new PdfMimePart(pdfConfig, envelope);
                        } catch (IOException e) {
                            log.error("Unable to read part file: {}", file);
                        }
                    }

                    // Make a file part if defined.
                    if (file != null) {
                        try {
                            mimePart = new FileMimePart(file);
                        } catch (IOException e) {
                            log.error("Unable to read part file: {}", file);
                        }
                    }

                    // Fallback to message or dummy else if file errors.
                    if (mimePart == null && message != null) {
                        if (session != null) {
                            message = session.magicReplace(message);
                        }
                        if (envelope != null) {
                            message = new String(envelope.envelopeMagicReplace(message.getBytes()));
                        }
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

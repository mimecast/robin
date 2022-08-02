package com.mimecast.robin.mime;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.mime.headers.MimeHeader;
import com.mimecast.robin.mime.parts.MimePart;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.session.Session;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.internet.MimeUtility;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Basic email MIME generator.
 */
public class EmailBuilder {
    static final Logger log = LogManager.getLogger(EmailBuilder.class);

    /**
     * Session instance.
     */
    protected final Session session;

    /**
     * MessageEnvelope instance.
     */
    protected final MessageEnvelope envelope;

    /**
     * List of headers.
     */
    protected final List<MimeHeader> headers = new ArrayList<>();

    /**
     * List of parts groupped type.
     */
    protected final List<MimePart> mixed = new ArrayList<>();
    protected final List<MimePart> related = new ArrayList<>();
    protected final List<MimePart> alternative = new ArrayList<>();

    /**
     * Constructs a new EmailBuilder instance with given Session and MessageEnvelope instance.
     *
     * @param session  Session instance.
     * @param envelope MessageEnvelope instance.
     */
    public EmailBuilder(Session session, MessageEnvelope envelope) {
        this.session = session;
        this.envelope = envelope;
        headers.add(new MimeHeader("MIME-Version", "1.0"));
    }

    /**
     * Builds email from MimeConfig on demand.
     *
     * @return Self.
     */
    public EmailBuilder buildMime() {
        if (envelope.getMime() != null && !envelope.getMime().isEmpty()) {
            envelope.getMime().getHeaders().forEach(h -> headers.add(new MimeHeader(h.getName(), session.magicReplace(h.getValue()))));

            for (MimePart part : envelope.getMime().getParts(session, envelope)) {
                if (part.getHeader("Content-ID") != null) {
                    related.add(part);

                } else if (part.getHeader("Content-Type").getCleanValue().startsWith("text/")) {
                    alternative.add(part);

                } else {
                    mixed.add(part);
                }
            }
        }

        return this;
    }

    /**
     * Adds header with given name and value.
     *
     * @param name  Header name.
     * @param value Header value.
     * @return Self.
     */
    public EmailBuilder addHeader(String name, String value) {
        try {
            boolean multiline = value.contains("\r") || value.contains("\n");
            headers.add(new MimeHeader(name,
                    multiline ?
                            "=?UTF-8?B?" + Base64.encodeBase64String(value.getBytes()).trim() + "?=" :
                            MimeUtility.encodeText(value)
            ));
        } catch (UnsupportedEncodingException e) {
            log.warn("Unable to encode header value: {}", e.getMessage());
            headers.add(new MimeHeader(name, value.replaceAll("\n", "\n\t")));
        }
        return this;
    }

    /**
     * Adds missing required headers.
     */
    private void addMissingHeaders() {
        List<String> addedHeaders = headers.stream()
                .map(h -> h.getName().toLowerCase())
                .collect(Collectors.toList());

        // Date
        if (!addedHeaders.contains("date")) {
            DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Config.getProperties().getLocale());
            headers.add(new MimeHeader("Date", dateFormat.format(new Date())));
        }

        // Message-ID
        String messageId = "<" + UUID.randomUUID() + ">";
        if (!addedHeaders.contains("message-id")) {
            headers.add(new MimeHeader("Message-ID", messageId));
        }

        // Subject
        if (!addedHeaders.contains("subject")) {
            headers.add(new MimeHeader("Subject", "Robin " + messageId));
        }

        // From
        if (!addedHeaders.contains("from")) {
            headers.add(new MimeHeader("From", "<" + (envelope.getMail() != null ? envelope.getMail() : "") + ">"));
        }

        // To
        if (!addedHeaders.contains("to")) {
            headers.add(new MimeHeader("To", "<" + String.join(">, <", envelope.getRcpts()) + ">"));
        }
    }

    /**
     * Adds part.
     *
     * @param part MimePart instance.
     * @return Self.
     */
    public EmailBuilder addPart(MimePart part) {
        MimeHeader contentType = part.getHeader("content-type");
        if (contentType != null && contentType.getValue().contains("text/") && !contentType.getValue().contains("name=")) {
            alternative.add(part);

        } else if (part.getHeader("content-id") != null) {
            related.add(part);

        } else {
            mixed.add(part);
        }

        return this;
    }

    /**
     * Writes email to given output stream.
     *
     * @param outputStream OutputStream instance.
     * @return Self.
     * @throws IOException Unable to write to output stream.
     */
    public EmailBuilder writeTo(OutputStream outputStream) throws IOException {
        addMissingHeaders();

        // Write haaders
        for (MimeHeader header : headers) {
            outputStream.write(header.toString().getBytes());
        }

        // Single part.
        if (mixed.size() + related.size() + alternative.size() == 1) {
            // Merge parts into one list for eacy extraction of part.
            List<MimePart> parts = new ArrayList<>(mixed);
            parts.addAll(related);
            parts.addAll(alternative);

            parts.get(0).writeTo(outputStream);
        }

        // Multipart.
        else {
            writeMultiparts(outputStream);
        }

        return this;
    }

    /**
     * Writes multiparts.
     *
     * @param outputStream OutputStream instance.
     * @throws IOException Unable to write to output stream.
     */
    @SuppressWarnings("java:S1192")
    private void writeMultiparts(OutputStream outputStream) throws IOException {
        if (!mixed.isEmpty()) {
            makeMultipart(outputStream, "mixed");
        }

        if (!related.isEmpty()) {
            makeMultipart(outputStream, "related");
        }

        if (!alternative.isEmpty()) {
            makeMultipart(outputStream, "alternative");
            writeMultipartParts(outputStream, alternative, "alternative");
        }

        if (!related.isEmpty()) {
            if (!alternative.isEmpty()) {
                outputStream.write(("--robin" + StringUtils.capitalize("related") + "\r\n").getBytes());
            }
            writeMultipartParts(outputStream, related, "related");
        }

        if (!mixed.isEmpty()) {
            if (!alternative.isEmpty() || !related.isEmpty()) {
                outputStream.write(("--robin" + StringUtils.capitalize("mixed") + "\r\n").getBytes());
            }
            writeMultipartParts(outputStream, mixed, "mixed");
        }
    }

    /**
     * Makes multipart.
     *
     * @param outputStream OutputStream instance.
     * @param type         Multipart type.
     * @throws IOException Unable to write to output stream.
     */
    private void makeMultipart(OutputStream outputStream, String type) throws IOException {
        String boundary = "robin" + StringUtils.capitalize(type);

        outputStream.write(("Content-Type: multipart/" + type + "; boundary=\"" + boundary + "\"\r\n").getBytes());
        outputStream.write("\r\n".getBytes());
        outputStream.write(("--" + boundary + "\r\n").getBytes());
    }

    /**
     * Writes multipart parts.
     *
     * @param outputStream OutputStream instance.
     * @param parts        List of mime parts.
     * @param type         Multipart type.
     * @throws IOException Unable to write to output stream.
     */
    private void writeMultipartParts(OutputStream outputStream, List<MimePart> parts, String type) throws IOException {
        String boundary = "robin" + StringUtils.capitalize(type);

        for (int i = 0; i < parts.size(); i++) {
            parts.get(i).writeTo(outputStream);
            outputStream.write(("--" + boundary + (i == parts.size() - 1 ? "--" : "") + "\r\n").getBytes());
        }
    }
}

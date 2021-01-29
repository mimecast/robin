package com.mimecast.robin.mime;

import com.mimecast.robin.mime.headers.MimeHeader;
import com.mimecast.robin.mime.headers.MimeHeaders;
import com.mimecast.robin.mime.parts.MimePart;
import com.mimecast.robin.smtp.io.LineInputStream;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic email MIME parser.
 */
public class EmailParser {

    /**
     * Email stream.
     */
    private final LineInputStream stream;

    /**
     * Parsed headers.
     */
    private final MimeHeaders headers = new MimeHeaders();

    /**
     * Parsed parts.
     */
    private final List<MimePart> parts = new ArrayList<>();

    /**
     * Constructs new EmailParser instance with given path.
     *
     * @param path Path to email.
     * @throws FileNotFoundException File not found.
     */
    public EmailParser(String path) throws FileNotFoundException {
        this.stream = new LineInputStream(new FileInputStream(path));
    }

    /**
     * Constructs new EmailParser instance with given path.
     *
     * @param stream Email LineInputStream instance.
     */
    public EmailParser(LineInputStream stream) {
        this.stream = stream;
    }

    /**
     * Parses email.
     *
     * @param headersOnly Parse only headers.
     * @return Self.
     * @throws IOException File read/write issue.
     */
    public EmailParser parse(boolean headersOnly) throws IOException {
        parseHeaders();

        if (!headersOnly) {
            parseBody();
        }

        return this;
    }

    /**
     * Parses email headers.
     *
     * @throws IOException File read/write issue.
     */
    private void parseHeaders() throws IOException {
        byte[] bytes;
        StringBuilder header = new StringBuilder();
        while ((bytes = stream.readLine()) != null) {

            String line = new String(bytes);

            // If line doens't start with a whitespace
            // we need to produce a header from what we got so far
            // if any.
            if (!Character.isWhitespace(bytes[0]) && header.length() > 0) {
                headers.put(new MimeHeader(header.toString()));
                header = new StringBuilder();
            }

            // Break if found end of headers.
            if (StringUtils.isBlank(line.trim())) {
                break;
            }

            header.append(line);
        }

        // Last header
        if (header.length() > 0) {
            headers.put(new MimeHeader(header.toString()));
        }
    }

    /**
     * Parses email body.
     */
    private void parseBody() {
        // TODO Parse body for parts.
    }

    /**
     * Gets headers.
     *
     * @return MimeHeaders instance.
     */
    public MimeHeaders getHeaders() {
        return headers;
    }

    /**
     * Gets parts.
     *
     * @return List of MimePart.
     */
    public List<MimePart> getParts() {
        return parts;
    }
}

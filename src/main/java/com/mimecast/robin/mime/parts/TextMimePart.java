package com.mimecast.robin.mime.parts;

import com.mimecast.robin.mime.headers.MimeHeader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * MIME part container from string.
 */
public class TextMimePart extends MimePart {

    /**
     * Constructs a new TextMimePart instance with given string.
     *
     * @param content Body content.
     */
    public TextMimePart(String content) {
        this.body = new ByteArrayInputStream(content.getBytes());
    }

    /**
     * Writes email to given output stream.
     *
     * @param outputStream OutputStream instance.
     * @return Self.
     * @throws IOException Unable to write to output stream.
     */
    @Override
    public MimePart writeTo(OutputStream outputStream) throws IOException {
        // Ensure we have a Content-Type header.
        MimeHeader contentType = getHeader("Content-Type");
        if (contentType == null) {
            headers.add(new MimeHeader("Content-Type", "text/plain; charset=\"ISO-8859-1\""));
        }

        super.writeTo(outputStream);

        return this;
    }
}

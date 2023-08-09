package com.mimecast.robin.mime.parts;

import com.mimecast.robin.mime.headers.MimeHeader;
import com.mimecast.robin.util.CharsetDetector;
import org.apache.commons.io.IOUtils;

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
    public TextMimePart(byte[] content) {
        body = new ByteArrayInputStream(content);
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
            headers.put(new MimeHeader("Content-Type", "text/plain; charset=\"UTF-8\""));
        }

        super.writeTo(outputStream);
        return this;
    }

    /**
     * Gets content.
     *
     * @return Content String.
     * @throws IOException Unable to read stream.
     */
    public String getContent() throws IOException {
        body.reset();
        byte[] bytes = IOUtils.toByteArray(body);

        if (content == null) {
            content = IOUtils.toString(bytes, CharsetDetector.getCharset(bytes));
        }

        return content;
    }
}

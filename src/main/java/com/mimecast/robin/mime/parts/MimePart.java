package com.mimecast.robin.mime.parts;

import com.mimecast.robin.mime.headers.MimeHeader;
import com.mimecast.robin.mime.headers.MimeHeaders;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.geronimo.mail.util.QuotedPrintableEncoderStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * MIME part container abstract.
 */
public abstract class MimePart {

    /**
     * Part headers.
     */
    protected final MimeHeaders headers = new MimeHeaders();

    /**
     * Part body as input stream.
     */
    protected InputStream body;

    /**
     * Adds header with given anme and value.
     *
     * @param name  Header name.
     * @param value Header value.
     * @return Self.
     */
    public MimePart addHeader(String name, String value) {
        headers.put(new MimeHeader(name, value));
        return this;
    }

    /**
     * Gets headers container.
     *
     * @return MimeHeaders instance.
     */
    public MimeHeaders getHeaders() {
        return headers;
    }

    /**
     * Gets header by name.
     * <p>Case insensitive.
     *
     * @param name Header name.
     * @return Self.
     */
    public MimeHeader getHeader(String name) {
        for (MimeHeader header : headers.get()) {
            if (header.getName().equalsIgnoreCase(name)) {
                return header;
            }
        }

        return null;
    }

    /**
     * Writes email to given output stream.
     *
     * @param outputStream OutputStream instance.
     * @return Self.
     * @throws IOException Unable to write to output stream.
     */
    public MimePart writeTo(OutputStream outputStream) throws IOException {
        // Write headers.
        for (MimeHeader header : headers.get()) {
            outputStream.write(header.toString().getBytes());
        }

        // Write separator.
        outputStream.write("\r\n".getBytes());

        // Wrap output stream with encoder if set.
        OutputStream writeStream = outputStream;

        MimeHeader contentEncoding = getHeader("Content-Transfer-Encoding");
        if (contentEncoding != null) {
            if (contentEncoding.getValue().toLowerCase().contains("quoted-printable")) {
                writeStream = new QuotedPrintableEncoderStream(outputStream);
            } else if (contentEncoding.getValue().toLowerCase().contains("base64")) {
                writeStream = new Base64OutputStream(outputStream);
            }
        }

        // Write content.
        IOUtils.copy(body, writeStream);

        // Ensure empty line at the end.
        outputStream.write("\r\n".getBytes());

        return this;
    }
}

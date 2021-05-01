package com.mimecast.robin.mime.parts;

import com.mimecast.robin.mime.headers.MimeHeader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * MIME part container from file path.
 */
public class FileMimePart extends MimePart {

    /**
     * Constructs a new FileMimePart instance.
     */
    public FileMimePart() {
        // Do nothing.
    }

    /**
     * Constructs a new FileMimePart instance with given path.
     *
     * @param path Path to file.
     * @throws IOException Unable to open/read from file.
     */
    public FileMimePart(String path) throws IOException {
        body = new FileInputStream(path);
    }

    /**
     * Writes email to given output stream.
     *
     * @param outputStream OutputStream instance.
     * @return Self.
     * @throws IOException Unable to open/read from file or write to output stream.
     */
    @Override
    public MimePart writeTo(OutputStream outputStream) throws IOException {
        // Ensure we have a Content-Type header.
        MimeHeader contentType = getHeader("Content-Type");
        if (contentType == null) {
            headers.put(new MimeHeader("Content-Type", "application/octet-stream"));
        }

        super.writeTo(outputStream);

        return this;
    }
}

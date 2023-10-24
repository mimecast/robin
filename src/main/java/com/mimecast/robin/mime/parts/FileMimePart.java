package com.mimecast.robin.mime.parts;

import com.google.common.primitives.Bytes;
import com.mimecast.robin.mime.headers.MimeHeader;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;

/**
 * MIME part container from file path.
 */
public class FileMimePart extends MimePart {

    /**
     * Append random bytes to the end of the file.
     */
    protected int appendRandomBytes = 0;

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
        int defaultBufferSize = 8192;
        body = new BufferedInputStream(new FileInputStream(path), defaultBufferSize);
        body.mark(defaultBufferSize);
    }

    /**
     * Constructs a new FileMimePart instance with given path and buffer size.
     *
     * @param path       Path to file.
     * @param bufferSize Buffer size.
     * @throws IOException Unable to open/read from file.
     */
    public FileMimePart(String path, int bufferSize) throws IOException {
        body = new BufferedInputStream(new FileInputStream(path), bufferSize);
        body.mark(bufferSize);
    }

    /**
     * Sets how many random bytes to append at the end of the file.
     *
     * @param size Number of random bytes to append.
     * @return Self.
     */
    public FileMimePart setAppendRandomBytes(int size) {
        appendRandomBytes = size;
        return this;
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

    /**
     * Gets content as bytes.
     *
     * @return String.
     * @throws IOException Unable to read stream.
     */
    @Override
    public byte[] getBytes() throws IOException {
        byte[] bytes = super.getBytes();

        if (appendRandomBytes > 0) {
            byte[] randomBytes = new byte[appendRandomBytes];
            new SecureRandom().nextBytes(randomBytes);

            bytes = Bytes.concat(bytes, randomBytes);
        }

        return bytes;
    }
}

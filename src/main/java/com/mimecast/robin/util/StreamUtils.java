package com.mimecast.robin.util;

import com.mimecast.robin.smtp.io.LineInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Stream utils.
 */
public class StreamUtils {

    /**
     * Protected constructor.
     */
    private StreamUtils() {
        throw new IllegalStateException("Static class");
    }

    /**
     * Parses lines into map of numbered lines.
     *
     * @param outputStream Stream to parse.
     * @return Map of Integer and String.
     * @throws IOException Unable to read from stream.
     */
    public static Map<Integer, String> parseLines(ByteArrayOutputStream outputStream) throws IOException {
        final Map<Integer, String> lines = new HashMap<>();

        LineInputStream stream = new LineInputStream(new ByteArrayInputStream(outputStream.toByteArray()));

        byte[] bytes;
        while ((bytes = stream.readLine()) != null) {
            lines.put(stream.getLineNumber(), new String(bytes));
        }

        return lines;
    }

    /**
     * Closes a Closeable unconditionally.
     *
     * @param closeable Object to close.
     */
    public static void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            // Ignore exception on purpose.
        }
    }
}

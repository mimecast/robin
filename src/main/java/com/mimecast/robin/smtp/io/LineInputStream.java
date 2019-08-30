package com.mimecast.robin.smtp.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * Line input stream.
 * <p>InputStream implementation returns lines with EOL as byte array and counts lines.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class LineInputStream extends PushbackInputStream {
    private static final Logger log = LogManager.getLogger(LineInputStream.class);

    /**
     * Carrige return byte.
     */
    private static final int CR = 13; // \r

    /**
     * Line feed byte.
     */
    private static final int LF = 10; // \n

    /**
     * Current line number.
     */
    private int lineNumber = 0;

    /**
     * Constructs a new LineInputStream instance.
     *
     * @param stream InputStream instance.
     */
    public LineInputStream(InputStream stream) {
        super(stream);
    }

    /**
     * Gets line number.
     *
     * @return Line number.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Read line as byte array.
     *
     * @return Byte array.
     * @throws IOException Unable to read.
     */
    @SuppressWarnings({"squid:S1168","squid:S135"})
    public byte[] readLine() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        boolean foundCR = false;
        int intByte;
        while((intByte = read()) != -1) {
            // Have CR but LF doesn't follow unread byte and return what is read so far.
            if (foundCR && intByte != LF) {
                unread(intByte);
                break;
            }

            // CR is recorded and read continues in hope of finding LF next.
            foundCR = foundCR || intByte == CR;

            // LF will instantly terminate the read and return.
            if (intByte == LF) {
                buffer.write(intByte);
                break;
            }

            buffer.write(intByte);
        }

        if (buffer.size() == 0) {
            log.debug("Buffer empty.");
            return null;
        }

        lineNumber++;
        return buffer.toByteArray();
    }
}

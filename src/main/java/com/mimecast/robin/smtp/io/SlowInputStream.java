package com.mimecast.robin.smtp.io;

import com.mimecast.robin.util.Sleep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * Slow input stream.
 *
 * <p>Slows down the reading for given miliseconds every given bytes.
 */
public class SlowInputStream extends InputStream {
    private static final Logger log = LogManager.getLogger(SlowInputStream.class);

    /**
     * Input stream instance.
     */
    private final InputStream in;

    /**
     * Size of bytes to wait after.
     */
    private final int bytes;

    /**
     * Time in miliseconds to wait for.
     */
    private final int wait;

    /**
     * Time in miliseconds waited for.
     */
    private int totalWait = 0;

    /**
     * Bytes read counter.
     */
    private int count = 0;

    /**
     * Constructs a new SlowInputStream instance with given bytes and wait.
     *
     * @param in    InputStream instance.
     * @param bytes Size of bytes.
     * @param wait  Time in miliseconds.
     */
    public SlowInputStream(InputStream in, int bytes, int wait) {
        this.in = in;
        this.bytes = bytes;
        this.wait = wait;
    }

    @Override
    public int read() throws IOException {
        if (bytes >= 128 && wait >= 100) {
            int read = in.read();

            count++;
            if (count == bytes) {
                count = 0;
                log.info("Waiting after {} bytes read.", bytes);
                totalWait += wait;
                Sleep.nap(wait);
            }

            return read;
        } else {
            return in.read();
        }
    }

    /**
     * Gets total wait time spent waiting in miliseconds.
     * <p>This is primarly here for unit testing.
     *
     * @return Integer.
     */
    public int getTotalWait() {
        return totalWait;
    }
}

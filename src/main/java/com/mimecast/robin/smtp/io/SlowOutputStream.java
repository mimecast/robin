package com.mimecast.robin.smtp.io;

import com.mimecast.robin.util.Sleep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Slow output stream.
 * <p>Slows down the writing for given miliseconds every given bytes.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
@SuppressWarnings("squid:S4349")
public class SlowOutputStream extends OutputStream {
    private static final Logger log = LogManager.getLogger(SlowOutputStream.class);

    /**
     * Output stream instance.
     */
    private final OutputStream out;

    /**
     * Size of bytes to wait after.
     */
    private final int bytes;

    /**
     * Time in miliseconds to wait for.
     */
    private final int wait;

    /**
     * Bytes read counter.
     */
    private int count = 0;

    /**
     * Constructs a new SlowOutputStream instance with given bytes and wait.
     *
     * @param out   OutputStream instance.
     * @param bytes Size of bytes.
     * @param wait Time out miliseconds.
     */
    public SlowOutputStream(OutputStream out, int bytes, int wait) {
        this.out = out;
        this.bytes = bytes;
        this.wait = wait;
    }

    @Override
    public void write(int b) throws IOException {
        if (bytes >= 128 && wait >= 100) {
            count++;
            if (count == bytes) {
                count = 0;
                log.info("Waiting after {} bytes wrote.",  bytes);
                Sleep.nap(wait);
            }
        }

        out.write(b);
    }
}

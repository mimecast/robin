package com.mimecast.robin.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Sleep utility.
 * <p>Some operations require a little bit of waiting.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class Sleep {
    private static final Logger log = LogManager.getLogger(Sleep.class);

    /**
     * Protected constructor.
     */
    private Sleep() {
        throw new IllegalStateException("Static class");
    }

    /**
     * Take a nap.
     *
     * @param delay Time in miliseconds.
     */
    public static void nap(int delay) {
        try {
            log.debug("Waiting {} miliseconds.", delay);
            Thread.sleep(delay); // Take a nap.
        } catch (InterruptedException e) {
            log.info("Interrupted.");
            Thread.currentThread().interrupt();
        }
    }
}

package com.mimecast.robin.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Sleep utility.
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
            log.info("Napping {} miliseconds.", delay);
            Thread.sleep(delay); // Take a nap.
        } catch (InterruptedException e) {
            log.info("Nap interrupted.");
            Thread.currentThread().interrupt();
        }
    }
}

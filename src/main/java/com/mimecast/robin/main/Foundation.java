package com.mimecast.robin.main;

import com.mimecast.robin.annotation.AnnotationLoader;
import com.mimecast.robin.config.ConfigLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.ConfigurationException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Run-once initializer for server and client.
 *
 * <p>Provides the runonce initializer for global configuration.
 * <p>Both server and client extend this.
 * <p>This loads the config files and annotations.
 *
 * @see Server
 * @see Client
 * @see ConfigLoader
 * @see AnnotationLoader
 */
public abstract class Foundation {
    protected static final Logger log = LogManager.getLogger(Foundation.class);

    /**
     * Run once boolean.
     */
    private static final AtomicBoolean runOnce = new AtomicBoolean();

    /**
     * Run once initializer.
     *
     * @param path Path to configuration file.
     * @throws ConfigurationException Unable to read/parse config file.
     */
    public static synchronized void init(String path) throws ConfigurationException {
        if (runOnce.get()) return;
        if (runOnce.compareAndSet(false, true)) {
            ConfigLoader.load(path);
            AnnotationLoader.load();
        }
    }
}

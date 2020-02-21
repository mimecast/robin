package com.mimecast.robin.main;

import com.mimecast.robin.annotation.AnnotationLoader;
import com.mimecast.robin.config.ConfigLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.ConfigurationException;

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
@SuppressWarnings("squid:S1118")
public abstract class Foundation {
    static final Logger log = LogManager.getLogger(Foundation.class);

    /**
     * Run once.
     */
    private static boolean runOnce = false;

    /**
     * Run once initializer.
     *
     * @param path Path to configuration file.
     * @throws ConfigurationException Unable to read/parse config file.
     */
    public static void init(String path) throws ConfigurationException {
        if (runOnce) return;
        runOnce = true;

        ConfigLoader.load(path);
        AnnotationLoader.load();
    }
}

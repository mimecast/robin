package com.mimecast.robin.config;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.util.PathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Static predefined config files loader (client, server, properties, log4j).
 */
public class ConfigLoader {
    private static final Logger log = LogManager.getLogger(ConfigLoader.class);

    /**
     * Protected constructor.
     */
    private ConfigLoader() {
        throw new IllegalStateException("Static class");
    }

    /**
     * Load global configuration files.
     *
     * @param path Path to configuration file.
     * @throws ConfigurationException Unable to read/parse config file.
     */
    public static synchronized void load(String path) throws ConfigurationException {
        log.debug("Working directory: {}", System.getProperty("user.dir"));

        if (path == null) path = "cfg" + File.separator;

        String propertiesFile = "properties.json";
        if (Config.getProperties().hasProperty("properties")) {
            propertiesFile = Config.getProperties().getStringProperty("properties");
        }

        String propertiesPath = Paths.get(path, propertiesFile).toString();
        if (PathUtils.isFile(propertiesPath)) {
            try {
                Config.initProperties(propertiesPath);
            } catch (IOException e) {
                log.fatal("Error reading {}.", propertiesFile);
                throw new ConfigurationException("Can't read " + propertiesFile + ".");
            }
        }

        String serverPath = Paths.get(path, "server.json").toString();
        if (PathUtils.isFile(serverPath)) {
            try {
                Config.initServer(serverPath);
            } catch (IOException e) {
                log.fatal("Error reading server.json.");
                throw new ConfigurationException("Can't read server.json.");
            }
        }

        String clientPath = Paths.get(path, "client.json").toString();
        if (PathUtils.isFile(clientPath)) {
            try {
                Config.initClient(clientPath);
            } catch (Exception e) {
                log.fatal("Error reading client.json.");
                throw new ConfigurationException("Can't read client.json.");
            }
        }

        String log4jFile = "log4j2.xml";
        if (Config.getProperties().hasProperty("log4j2")) {
            log4jFile = Config.getProperties().getStringProperty("log4j2");
        }

        String log4jPath = Paths.get(path, log4jFile).toString();
        if (PathUtils.isFile(log4jPath)) {
            Configurator.initialize("Robin", log4jPath);
        }
    }
}

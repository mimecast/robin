package com.mimecast.robin.config;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.util.PathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * Static configuration files loader.
 * <p>Loads both client and server configuration files regardless of the invoked functionality.
 * <p>It also loads properties and log4j configuration.
 * <p>Make sure all files are in the default or given path.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
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
     */
    public static void load(String path) throws ConfigurationException {
        log.debug("Working directory: {}", System.getProperty("user.dir"));

        if (path == null) path = "cfg" + PathUtils.separator;

        String propertiesPath;
        String serverPath;
        String clientPath;
        String log4jPath;

        try {
            propertiesPath = PathUtils.validatePath(Paths.get(path, "properties.json").toString(), "properties.json");
            serverPath = PathUtils.validatePath(Paths.get(path, "server.json").toString(), "server.json");
            clientPath = PathUtils.validatePath(Paths.get(path, "client.json").toString(), "client.json");
            log4jPath = PathUtils.validatePath(Paths.get(path, "log4j2.xml").toString(), "log4j2.xml");
            log.debug("Configuration directory: {}", path);
        } catch (IOException e) {
            log.fatal("Error reading configuration file: {}", e.getMessage());
            throw new ConfigurationException("Can't find configuration file: " + e.getMessage());
        }

        try {
            Config.initProperties(propertiesPath);
        } catch (IOException e) {
            log.fatal("Error reading properties.json.");
            throw new ConfigurationException("Can't read properties.json.");
        }

        try {
            Config.initServer(serverPath);
        } catch (IOException e) {
            log.fatal("Error reading server.json.");
            throw new ConfigurationException("Can't read server.json.");
        }

        try {
            Config.initClient(clientPath);
        } catch (Exception e) {
            log.fatal("Error reading client.json.");
            throw new ConfigurationException("Can't read client.json.");
        }

        if (PathUtils.isFile(log4jPath)) {
            try {
                LoggerContext.getContext().setConfigLocation(new URI(log4jPath));
            } catch (URISyntaxException e) {
                log.fatal("Error loading log4j2.xml.");
                throw new ConfigurationException("Can't load log4j.xml.");
            }
        } else {
            log.fatal("Error reading log4j2.xml.");
            throw new ConfigurationException("Can't read log4j.xml.");
        }
    }
}

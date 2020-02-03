package com.mimecast.robin.main;

import com.mimecast.robin.config.client.ClientConfig;
import com.mimecast.robin.config.server.ServerConfig;

import java.io.IOException;

/**
 * Accessor for global configuration instances.
 * <p>Properties is designed to provide a generic container for extendability.
 * <p>This can also be used to access syetm properties.
 * <p>It will favor system properties over properties file values.
 *
 * <p>ServerConfig configuration holds the server config, authentication users and behaviour scenarios.
 * <p>ClientConfig configuration holds the client defaults and routes.
 *
 * @see Properties
 * @see ServerConfig
 * @see ClientConfig
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class Config {

    /**
     * Protected constructor.
     */
    private Config() {
        throw new IllegalStateException("Static class");
    }

    /**
     * SystemProperties or properties file configuration container.
     */
    private static Properties properties = new Properties();

    /**
     * Server configuration.
     */
    private static ServerConfig server = new ServerConfig();

    /**
     * Client default configuration.
     */
    private static ClientConfig client = new ClientConfig();

    /**
     * Gets properties.
     *
     * @return Properties.
     */
    public static Properties getProperties() {
        return properties;
    }

    /**
     * Init properties.
     *
     * @param path File path.
     * @throws IOException Unable to read file.
     */
    public static void initProperties(String path) throws IOException {
        properties = new Properties(path);
    }

    /**
     * Gets server config.
     *
     * @return ServerConfig.
     */
    public static ServerConfig getServer() {
        return server;
    }

    /**
     * Init server config.
     *
     * @param path File path.
     * @throws IOException Unable to read file.
     */
    public static void initServer(String path) throws IOException {
        server = new ServerConfig(path);
    }

    /**
     * Gets client config.
     *
     * @return ClientConfig.
     */
    public static ClientConfig getClient() {
        return client;
    }

    /**
     * Init client config.
     *
     * @param path File path.
     * @throws IOException Unable to read file.
     */
    public static void initClient(String path) throws IOException {
        client = new ClientConfig(path);
    }
}

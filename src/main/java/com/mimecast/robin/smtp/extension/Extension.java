package com.mimecast.robin.smtp.extension;

import com.mimecast.robin.smtp.extension.client.ClientProcessor;
import com.mimecast.robin.smtp.extension.server.ServerProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

/**
 * Extension container.
 *
 * <p>This holds pairs of client and server callable for the extension implementations.
 */
public class Extension {
    private static final Logger log = LogManager.getLogger(Extension.class);

    /**
     * Server callable.
     */
    private final Callable<ServerProcessor> server;

    /**
     * Client callable.
     */
    private final Callable<ClientProcessor> client;

    /**
     * Constructs a new Extension instance.
     *
     * @param server Server callable.
     * @param client Client callable.
     */
    public Extension(Callable<ServerProcessor> server, Callable<ClientProcessor> client) {
        this.server = server;
        this.client = client;
    }

    /**
     * Gets server.
     *
     * @return Server instance.
     */
    public ServerProcessor getServer() {
        try {
            return server.call();
        } catch (Exception e) {
            log.error("Error calling server for extension: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Gets client.
     *
     * @return Client instance.
     */
    public ClientProcessor getClient() {
        try {
            return client.call();
        } catch (Exception e) {
            log.error("Error calling client for extension: {}", e.getMessage());
        }
        return null;
    }
}

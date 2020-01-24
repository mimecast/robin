package com.mimecast.robin.config.server;

import com.mimecast.robin.config.ConfigFoundation;

import java.util.Map;

/**
 * Server user configuration container.
 * <p>This is a container for users defined in the server configuration.
 * <p>One instance will be made for every user defined.
 * <p>This can be used to authenticate users when testing clients.
 * <p>The server supports AUTH PLAIN LOGIN.
 *
 * @see ServerConfig
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class UserConfig extends ConfigFoundation {

    /**
     * Constructs a new UserConfig instance with given map.
     *
     * @param map Properties map.
     */
    @SuppressWarnings("rawtypes")
    public UserConfig(Map map) {
        super(map);
    }

    /**
     * Gets username.
     *
     * @return Username string.
     */
    public String getName() {
        return getStringProperty("name");
    }

    /**
     * Gets password.
     *
     * @return Password string.
     */
    public String getPass() {
        return getStringProperty("pass");
    }
}

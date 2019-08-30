package com.mimecast.robin.config.server;

import com.mimecast.robin.config.ConfigFoundation;

import java.io.IOException;
import java.util.*;

/**
 * Server configuration.
 * <p>This class provides type safe access to server configuration.
 * <p>It also maps authentication users and behaviour scenarios to corresponding objects.
 *
 * @see UserConfig
 * @see ScenarioConfig
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
@SuppressWarnings("unchecked")
public class ServerConfig extends ConfigFoundation {

    /**
     * Constructs a new ServerConfig instance with configuration path.
     *
     * @param path Path to configuration file.
     * @throws IOException Unable to read file.
     */
    public ServerConfig(String path) throws IOException {
        super(path);
    }

    /**
     * Gets bind address.
     *
     * @return Bind address string.
     */
    public String getBind() {
        return getStringProperty("bind");
    }

    /**
     * Gets bind port.
     *
     * @return Bind address number.
     */
    public int getPort() {
        return Math.toIntExact(getLongProperty("port"));
    }

    /**
     * Gets backlog size.
     *
     * @return Backlog size.
     */
    public int getBacklog() {
        return Math.toIntExact(getLongProperty("backlog"));
    }


    /**
     * Gets error limit.
     * <p>This defines how many syntax errors should be permitted before iterrupting the receipt.
     *
     * @return Error limit.
     */
    public int getErrorLimit() {
        return Math.toIntExact(getLongProperty("errorLimit", 3L));
    }

    /**
     * Is AUTH enabled.
     *
     * @return Boolean.
     */
    public boolean isAuth() {
        return getBooleanProperty("auth");
    }

    /**
     * Is STARTTLS enabled.
     *
     * @return Boolean.
     */
    public boolean isStartTls() {
        return getBooleanProperty("starttls");
    }

    /**
     * Is CHUNKING enabled.
     *
     * @return Boolean.
     */
    public boolean isChunking() {
        return getBooleanProperty("chunking");
    }

    /**
     * Gets key store.
     *
     * @return Key store path.
     */
    public String getKeyStore() {
        return getStringProperty("keystore");
    }

    /**
     * Gets key store password.
     *
     * @return Key store password string.
     */
    public String getKeyStorePassword() {
        return getStringProperty("keystorepassword");
    }

    /**
     * Gets users list.
     *
     * @return Users list.
     */
    public List<UserConfig> getUsers() {
        List<UserConfig> users = new ArrayList<>();
        for (Map<String, String> user : (List<Map<String, String>>) getListProperty("users")) {
            users.add(new UserConfig(user));
        }
        return users;
    }

    /**
     * Gets user by username.
     *
     * @param find Username to find.
     * @return Optional of UserConfig.
     */
    public Optional<UserConfig> getUser(String find) {
        for (UserConfig user : getUsers()) {
            if (user.getName().equals(find)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets scenarios map.
     *
     * @return Scenarios map.
     */
    public Map<String, ScenarioConfig> getScenarios() {
        Map<String, ScenarioConfig> scenarios = new HashMap<>();
        for (Object object : getMapProperty("scenarios").entrySet()) {
            Map.Entry entry = (Map.Entry) object;
            scenarios.put((String) entry.getKey(), new ScenarioConfig((Map) entry.getValue()));
        }
        return scenarios;
    }
}

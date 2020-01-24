package com.mimecast.robin.config.client;

import com.mimecast.robin.config.ConfigFoundation;

import java.util.List;
import java.util.Map;

/**
 * Client route configuration container.
 * <p>This is a container for routes defined in the client configuration.
 * <p>One instance will be made for every route defined.
 * <p>This can be used to configure MX, PORT and SMTP AUTH authentication in cases.
 *
 * @see ClientConfig
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
@SuppressWarnings("unchecked")
public class RouteConfig extends ConfigFoundation {

    /**
     * Constructs a new RouteConfig instance with given map.
     *
     * @param map Properties map.
     */
    @SuppressWarnings("rawtypes")
    public RouteConfig(Map map) {
        super(map);
    }

    /**
     * Gets route name.
     *
     * @return Name string.
     */
    public String getName() {
        return getStringProperty("name");
    }

    /**
     * Gets MX.
     *
     * @return MX list of string.
     */
    public List<String> getMx() {
        return getListProperty("mx");
    }

    /**
     * Gets port.
     *
     * @return Port number.
     */
    public int getPort() {
        return Math.toIntExact(getLongProperty("port"));
    }

    /**
     * Is authentication enabled.
     *
     * @return Boolean.
     */
    public boolean isAuth() {
        return getBooleanProperty("auth");
    }

    /**
     * Is AUTH LOGIN combined username and password login enabled.
     *
     * @return Boolean.
     */
    public boolean isAuthLoginCombined() {
        return getBooleanProperty("authLoginCombined");
    }

    /**
     * Is AUTH LOGIN retry enabled.
     *
     * @return Boolean.
     */
    public boolean isAuthLoginRetry() {
        return getBooleanProperty("authLoginRetry");
    }

    /**
     * Gets the username.
     *
     * @return Username string.
     */
    public String getUser() {
        return getStringProperty("user");
    }

    /**
     * Gets the password.
     *
     * @return password string.
     */
    public String getPass() {
        return getStringProperty("pass");
    }
}

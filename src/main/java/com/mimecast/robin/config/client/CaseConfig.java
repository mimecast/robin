package com.mimecast.robin.config.client;

import com.mimecast.robin.config.ConfigFoundation;
import com.mimecast.robin.config.assertion.AssertConfig;
import com.mimecast.robin.main.Config;

import java.io.IOException;
import java.util.*;

/**
 * Client case configuration container.
 *
 * <p>Running cases is the primary scope of this project.
 * <p>These are designed to be used for smoke testing your MTA.
 * <p>A case is configured via a JSON file.
 * <p>This class provides type safe access to the client case configuration.
 * <p>Cases will inherit defaults from ClientConfig.
 * <p>It also maps envelopes and assertions to corresponding objects.
 *
 * @see ClientConfig
 * @see ConfigFoundation
 */
@SuppressWarnings("unchecked")
public class CaseConfig extends ConfigFoundation {

    /**
     * String constant.
     */
    private static final String ROUTE = "route";

    /**
     * Constructs a new CaseConfig instance.
     * <p>Client configuration defaults will fill in by default.
     */
    public CaseConfig() {
        map = new HashMap<>();
        map.putAll(Config.getClient().getMap());
    }

    /**
     * Constructs a new CaseConfig instance with given configuration path.
     * <p>Client configuration defaults will fill in the blanks.
     *
     * @param path Path to configuration file.
     * @throws IOException Unable to read file.
     */
    public CaseConfig(String path) throws IOException {
        super(path);
        Config.getClient().getMap().forEach(map::putIfAbsent);
    }

    /**
     * Constructs a new CaseConfig instance with given map.
     *
     * @param map Configuration map.
     */
    @SuppressWarnings("rawtypes")
    public CaseConfig(Map map) {
        super(map);
    }

    /**
     * Gets retry count.
     *
     * @return Retry count.
     */
    public int getRetry() {
        return Math.toIntExact(getLongProperty("retry"));
    }

    /**
     * Gets retry delay in seconds.
     *
     * @return Retry delay.
     */
    public int getDelay() {
        return Math.toIntExact(getLongProperty("delay"));
    }

    /**
     * Gets socket timeout.
     * <p>In seconds.
     *
     * @return Socket timeout.
     */
    public int getTimeout() {
        return (int) Math.max(getLongProperty("timeout"), 0);
    }

    /**
     * Gets MX.
     * <p>Route mx supersedes case mx.
     *
     * @return MX list of string.
     */
    public List<String> getMx() {
        RouteConfig routeConfig = getRoute();
        return routeConfig != null && routeConfig.getMx() != null ?
                routeConfig.getMx() :
                getListProperty("mx", Collections.singletonList("127.0.0.1"));
    }

    /**
     * Gets port.
     * <p>Route port supersedes case port.
     *
     * @return Port number.
     */
    public int getPort() {
        RouteConfig routeConfig = getRoute();
        return routeConfig != null && routeConfig.getPort() > 0 ?
                routeConfig.getPort() :
                Math.toIntExact(getLongProperty("port", 25L));
    }

    /**
     * Gets behaviour.
     *
     * @return List of String.
     */
    public List<String> getBehaviour() {
        return getListProperty("behaviour");
    }

    /**
     * Is authentication enabled.
     * <p>Route auth supersedes case auth.
     *
     * @return Boolean.
     */
    public boolean isAuth() {
        RouteConfig routeConfig = getRoute();
        return routeConfig != null && routeConfig.isAuth() ||
                getBooleanProperty("auth", false);
    }

    /**
     * Is AUTH LOGIN combined username and password login enabled.
     * <p>Some services do not support this syntax.
     * <p>Route authLoginCombined supersedes case authLoginCombined.
     *
     * @return Boolean.
     */
    public boolean isAuthLoginCombined() {
        RouteConfig routeConfig = getRoute();
        return routeConfig != null && routeConfig.isAuthLoginCombined() ||
                getBooleanProperty("authLoginCombined", false);
    }

    /**
     * Is AUTH LOGIN retry enabled.
     * <p>Retry disabled combined login.
     * <p>Will only work if the server has not severed the connection after first failure.
     * <p>Route authLoginRetry supersedes case authLoginRetry.
     *
     * @return Boolean.
     */
    public boolean isAuthLoginRetry() {
        RouteConfig routeConfig = getRoute();
        return routeConfig != null && routeConfig.isAuthLoginRetry() ||
                getBooleanProperty("authLoginRetry", false);
    }

    /**
     * Gets username for authentication.
     * <p>Route user supersedes case user.
     *
     * @return Username.
     */
    public String getUser() {
        RouteConfig routeConfig = getRoute();
        return routeConfig != null && routeConfig.getUser() != null ?
                routeConfig.getUser() :
                getStringProperty("user");
    }

    /**
     * Gets password for authentication.
     * <p>Route pass supersedes case pass.
     *
     * @return Password.
     */
    public String getPass() {
        RouteConfig routeConfig = getRoute();
        return routeConfig != null && routeConfig.getPass() != null ?
                routeConfig.getPass() :
                getStringProperty("pass");
    }

    /**
     * Gets route config if defined.
     *
     * @return RouteConfig instance.
     */
    private RouteConfig getRoute() {
        return hasProperty(ROUTE) ?
                Config.getClient().getRoute(getStringProperty(ROUTE)) :
                null;
    }

    /**
     * Is TLS.
     *
     * @return Boolean.
     */
    public boolean isTls() {
        return getBooleanProperty("tls", true);
    }

    /**
     * Is AUTH before TLS.
     *
     * @return Boolean.
     */
    public boolean isAuthBeforeTls() {
        return getBooleanProperty("authBeforeTls", false);
    }

    /**
     * Gets TLS protocols to enable.
     *
     * @return Allowed protocols.
     */
    public String[] getProtocols() {
        List<String> protocols = getListProperty("protocols");
        if (protocols != null && !protocols.isEmpty()) {
            return protocols.toArray(new String[0]);
        }

        return new String[0];
    }

    /**
     * Gets TLS ciphers to enable.
     *
     * @return Allowed ciphers.
     */
    public String[] getCiphers() {
        List<String> ciphers = getListProperty("ciphers");
        if (ciphers != null && !ciphers.isEmpty()) {
            return ciphers.toArray(new String[0]);
        }

        return new String[0];
    }

    /**
     * Gets EHLO domain.
     *
     * @return Ehlo domain.
     */
    public String getEhlo() {
        return getStringProperty("ehlo", "localhost");
    }

    /**
     * Gets MAIL FROM address.
     *
     * @return Email address.
     */
    public String getMail() {
        return getStringProperty("mail");
    }

    /**
     * Gets RCPT TO address.
     *
     * @return Email address.
     */
    public List<String> getRcpt() {
        return getListProperty("rcpt");
    }

    /**
     * Gets envelopes list.
     *
     * @return Envelopes list.
     */
    public List<EnvelopeConfig> getEnvelopes() {
        List<EnvelopeConfig> envelopes = new ArrayList<>();
        if (hasProperty("envelopes")) {
            for (Map<String, String> user : (List<Map<String, String>>) getListProperty("envelopes")) {
                envelopes.add(new EnvelopeConfig(user));
            }
        }

        return envelopes;
    }

    /**
     * Gets assertions.
     *
     * @return AssertConfig instance.
     */
    public AssertConfig getAssertions() {
        return new AssertConfig(getMapProperty("assertions"));
    }
}

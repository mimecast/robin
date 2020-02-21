package com.mimecast.robin.config.client;

import com.mimecast.robin.config.ConfigFoundation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default client configuration container.
 *
 * <p>This class provides type safe access to default client configuration.
 * <p>Cases inherit defaults from here.
 * <p>This also houses routes that can be chosen in a case.
 *
 * @see ConfigFoundation
 */
@SuppressWarnings("unchecked")
public class ClientConfig extends ConfigFoundation {
    private final List<RouteConfig> routes = new ArrayList<>();

    /**
     * Constructs a new ClientConfig instance.
     */
    public ClientConfig() {
        super();
    }

    /**
     * Constructs a new ClientConfig instance with configuration path.
     *
     * @param path Path to configuration file.
     * @throws IOException Unable to read file.
     */
    public ClientConfig(String path) throws IOException {
        super(path);
    }

    /**
     * Gets MAIL property.
     *
     * @return MAIL string.
     */
    public String getMail() {
        return getStringProperty("mail");
    }

    /**
     * Gets RCPT property.
     *
     * @return RCPT string.
     */
    public List<String> getRcpt() {
        return getListProperty("rcpt");
    }

    /**
     * Gets route if any.
     *
     * @param name Route name.
     * @return Route configuration instance.
     */
    @SuppressWarnings({"RedundantCast", "rawtypes"})
    public RouteConfig getRoute(String name) {
        if (routes.isEmpty()) getListProperty("routes").forEach(map -> routes.add(new RouteConfig((Map) map)));
        return routes.stream().filter(route -> route.getName().equals(name)).findFirst().orElse(null);
    }
}

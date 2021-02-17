package com.mimecast.robin.config;

import com.google.gson.Gson;
import com.mimecast.robin.config.client.ClientConfig;
import com.mimecast.robin.config.server.ServerConfig;
import com.mimecast.robin.util.PathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON config file loader and primitive getters provider.
 *
 * <p>This provides the default means to get lists, maps, strings, longs and booleans.
 * <p>Lists and maps are unchecked and should be checked in the extending classes.
 *
 * @see Properties
 * @see ServerConfig
 * @see ClientConfig
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class ConfigFoundation {
    protected static final Logger log = LogManager.getLogger(ConfigFoundation.class);

    /**
     * Properties container.
     */
    protected Map<String, Object> map = new HashMap<>();

    /**
     * Constructs a new ConfigFoundation instance with given file path.
     *
     * @param path File path.
     * @throws IOException Unable to read file.
     */
    public ConfigFoundation(String path) throws IOException {
        if (PathUtils.isFile(path)) {
            String props = PathUtils.readFile(path, Charset.defaultCharset());
            map = new Gson().fromJson(props, Map.class);
        }
    }

    /**
     * Constructs a new ConfigFoundation instance.
     */
    protected ConfigFoundation() {
        // Clean initialisation.
    }

    /**
     * Constructs a new ConfigFoundation instance with given map.
     *
     * @param map Map.
     */
    protected ConfigFoundation(Map map) {
        this.map = map;
    }

    /**
     * Gets instance properties map.
     *
     * @return Map.
     */
    public Map<String, Object> getMap() {
        return map;
    }

    /**
     * Check a property exists.
     *
     * @param name Property name.
     * @return Boolean.
     */
    public boolean hasProperty(String name) {
        return map.containsKey(name);
    }

    /**
     * Gets String property.
     *
     * @param name Property name.
     * @return String.
     */
    public String getStringProperty(String name) {
        return (String) map.get(name);
    }

    /**
     * Gets String property with default.
     *
     * @param name Property name.
     * @param def  Default value.
     * @return String.
     */
    public String getStringProperty(String name, String def) {
        return hasProperty(name) ? getStringProperty(name) : def;
    }

    /**
     * Gets Long property.
     *
     * @param name Property name.
     * @return Long.
     */
    public Long getLongProperty(String name) {
        Object prop = map.get(name);
        if (prop != null) {
            if (prop instanceof Integer) {
                return ((Integer) prop).longValue();
            }
            if (prop instanceof Long) {
                return (Long) prop;
            }
            if (prop instanceof Short) {
                return ((Short) prop).longValue();
            }
            return prop instanceof Double ? ((Double) prop).longValue() : Long.parseLong((String) map.get(name));
        }
        return 0L;
    }

    /**
     * Gets Long property with default.
     *
     * @param name Property name.
     * @param def  Default value.
     * @return Long.
     */
    public Long getLongProperty(String name, Long def) {
        return hasProperty(name) ? getLongProperty(name) : def;
    }

    /**
     * Gets Boolean property.
     *
     * @param name Property name.
     * @return Boolean.
     */
    public Boolean getBooleanProperty(String name) {
        return map.get(name) != null && (Boolean) map.get(name);
    }

    /**
     * Gets Boolean property with default.
     *
     * @param name Property name.
     * @param def  Default value.
     * @return Boolean.
     */
    public Boolean getBooleanProperty(String name, Boolean def) {
        return hasProperty(name) ? getBooleanProperty(name) : def;
    }

    /**
     * Gets List property.
     *
     * @param name Property name.
     * @return List.
     */
    public List getListProperty(String name) {
        return map.containsKey(name) ? (List) map.get(name) : new ArrayList<>();
    }

    /**
     * Gets List property with default.
     *
     * @param name Property name.
     * @param def  Default value.
     * @return List.
     */
    public List getListProperty(String name, List def) {
        return hasProperty(name) ? getListProperty(name) : def;
    }

    /**
     * Gets Map property.
     *
     * @param name Property name.
     * @return Map.
     */
    public Map getMapProperty(String name) {
        return map.containsKey(name) ? (Map) map.get(name) : new HashMap<>();
    }

    /**
     * Gets Map property with default.
     *
     * @param name Property name.
     * @param def  Default value.
     * @return Map.
     */
    public Map getMapProperty(String name, Map def) {
        return hasProperty(name) ? getMapProperty(name) : def;
    }

    /**
     * Is empty.
     *
     * @return Boolean.
     */
    public boolean isEmpty() {
        return map == null || map.isEmpty();
    }
}

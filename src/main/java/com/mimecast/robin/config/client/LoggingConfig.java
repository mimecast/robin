package com.mimecast.robin.config.client;

import com.mimecast.robin.config.ConfigFoundation;

import java.util.Map;

/**
 * Configuration foundation.
 */
public class LoggingConfig extends ConfigFoundation {

    /**
     * Constructs a new MtaUid instance with given map.
     *
     * @param map Map.
     */
    public LoggingConfig(Map<String, Object> map) {
        super(map);
    }

    /**
     * Gets data boolean.
     *
     * @return Boolean.
     */
    public boolean getData() {
        return getBooleanProperty("data", true);
    }
}

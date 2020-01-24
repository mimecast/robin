package com.mimecast.robin.config;

import java.util.Map;

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
     */
    public boolean getData() {
        return getBooleanProperty("data", true);
    }
}

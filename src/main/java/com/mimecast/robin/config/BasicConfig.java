package com.mimecast.robin.config;

import java.util.Map;

/**
 * Config generic implemetation.
 */
public class BasicConfig extends ConfigFoundation {

    /**
     * Constructs a new BasicConfig instance with given map.
     *
     * @param map Configuration map.
     */
    @SuppressWarnings("rawtypes")
    public BasicConfig(Map map) {
        super(map);
    }
}

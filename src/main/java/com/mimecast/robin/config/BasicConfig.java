package com.mimecast.robin.config;

import java.util.Map;

/**
 * Config generic implemetation.
 */
public class BasicConfig extends ConfigFoundation {

    /**
     * Constructs a new BasicConfig instance.
     *
     * @param map Configuration map.
     */
    @SuppressWarnings("rawtypes")
    public BasicConfig(Map map) {
        super(map);
    }
}

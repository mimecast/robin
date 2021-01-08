package com.mimecast.robin.config;

import java.util.Map;

/**
 * Config generic implemetation.
 */
public class Config extends ConfigFoundation {

    /**
     * Constructs a new BasicConfig instance.
     *
     * @param map Configuration map.
     */
    @SuppressWarnings("rawtypes")
    public Config(Map map) {
        super(map);
    }
}

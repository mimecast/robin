package com.mimecast.robin.config;

import java.io.IOException;
import java.util.Map;

/**
 * Config generic implemetation.
 */
public class BasicConfig extends ConfigFoundation {

    /**
     * Constructs a new BasicConfig instance with given path.
     *
     * @param path Configuration pathj.
     * @throws IOException Unable to read file.
     */
    @SuppressWarnings("rawtypes")
    public BasicConfig(String path) throws IOException {
        super(path);
    }

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

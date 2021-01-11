package com.mimecast.robin.config;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicConfigTest {

    @Test
    void numbers() {
        Map<String, Object> map = new HashMap<>();
        map.put("integer", 7);
        map.put("double", 7D);
        map.put("short", (short) 7);
        map.put("long", 7L);
        map.put("string", "7");

        BasicConfig config = new BasicConfig(map);
        assertEquals(7, config.getLongProperty("integer"));
        assertEquals(7, config.getLongProperty("double"));
        assertEquals(7, config.getLongProperty("short"));
        assertEquals(7, config.getLongProperty("long"));
        assertEquals(7, config.getLongProperty("string"));
    }
}

package com.mimecast.robin.config;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void getBooleanProperty() {
        assertTrue(Config.getProperties().hasProperty("boolean"));
        assertTrue(Config.getProperties().getBooleanProperty("boolean"));
    }

    @Test
    void getLongProperty() {
        assertTrue(Config.getProperties().hasProperty("long"));
        assertEquals((Long) 7L, Config.getProperties().getLongProperty("long"));
    }

    @Test
    void getStringProperty() {
        assertTrue(Config.getProperties().hasProperty("string"));
        assertEquals("string", Config.getProperties().getStringProperty("string"));
    }

    @Test
    void getStringSubProperty() {
        assertEquals("substring", Config.getProperties().getStringProperty("sub.string"));
    }

    @Test
    void getListProperty() {
        assertTrue(Config.getProperties().hasProperty("list"));
        assertEquals("[monkey, weasel, dragon]", Config.getProperties().getListProperty("list").toString());
    }

    @Test
    void getMapProperty() {
        assertTrue(Config.getProperties().hasProperty("map"));
        assertEquals(1, Config.getProperties().getMapProperty("map").size());
        assertEquals("map", Config.getProperties().getMapProperty("map").get("string"));
    }

    @Test
    void getPropertyWithDefault() {
        assertFalse(Config.getProperties().hasProperty("default"));
        assertEquals("value", Config.getProperties().getStringProperty("default", "value"));
    }
}

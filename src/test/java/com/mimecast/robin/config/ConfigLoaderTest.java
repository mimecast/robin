package com.mimecast.robin.config;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Foundation;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigLoaderTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void load() {
        assertNotNull(Config.getServer());
        assertNotNull(Config.getClient());
        assertNotNull(Config.getProperties());
        assertEquals("src/test/resources/log4j2.xml", LoggerContext.getContext().getConfigLocation().getPath());
    }
}

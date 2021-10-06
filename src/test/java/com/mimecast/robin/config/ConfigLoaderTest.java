package com.mimecast.robin.config;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Foundation;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(LoggerContext.getContext().getConfiguration().getName().endsWith("/log4j2.xml"));
    }
}

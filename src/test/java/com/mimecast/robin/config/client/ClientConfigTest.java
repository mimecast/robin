package com.mimecast.robin.config.client;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

import static org.junit.jupiter.api.Assertions.*;

class ClientConfigTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void getMail() {
        assertEquals("tony@example.com", Config.getClient().getMail());
    }

    @Test
    void getRcpt() {
        assertTrue(String.join(", ", Config.getClient().getRcpt()).contains("pepper@example.com"));
    }

    @Test
    void getRoute() {
        // Tested in RouteConfigTest
        assertNotNull( Config.getClient().getRoute("com"));
    }
}

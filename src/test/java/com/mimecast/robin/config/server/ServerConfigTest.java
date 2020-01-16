package com.mimecast.robin.config.server;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

import static org.junit.jupiter.api.Assertions.*;

class ServerConfigTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void getBind() {
        assertEquals("::", Config.getServer().getBind());
    }

    @Test
    void getPort() {
        assertEquals(25, Config.getServer().getPort());
    }

    @Test
    void getBacklog() {
        assertEquals(20, Config.getServer().getBacklog());
    }

    @Test
    void getErrorLimit() {
        assertEquals(3, Config.getServer().getErrorLimit());
    }

    @Test
    void isAuth() {
        assertTrue(Config.getServer().isAuth());
    }

    @Test
    void isStartTls() {
        assertTrue(Config.getServer().isStartTls());
    }

    @Test
    void isChunking() {
        assertTrue(Config.getServer().isChunking());
    }

    @Test
    void getKeyStore() {
        assertEquals("src/test/resources/keystore.jks", Config.getServer().getKeyStore());
    }

    @Test
    void getKeyStorePassword() {
        assertEquals("avengers", Config.getServer().getKeyStorePassword());
    }

    @Test
    void getUsers() {
        assertEquals(1, Config.getServer().getUsers().size());
    }

    @Test
    void getUser() {
        // Tested in UserConfigTest.
        assertTrue(Config.getServer().getUser("tony@example.com").isPresent());
    }

    @Test
    void getScenarios() {
        // Tested in ScenarioConfigTest.
        assertFalse(Config.getServer().getScenarios().isEmpty());
    }
}

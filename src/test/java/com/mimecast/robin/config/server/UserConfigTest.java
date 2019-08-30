package com.mimecast.robin.config.server;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class UserConfigTest {

    private static UserConfig userConfig;

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");

        userConfig = Config.getServer().getUser("tony@example.com").get();
    }

    @Test
    void getName() {
        assertEquals("tony@example.com", userConfig.getName());
    }

    @Test
    void getPass() {
        assertEquals("giveHerTheRing", userConfig.getPass());
    }
}

package com.mimecast.robin.util;

import com.mimecast.robin.smtp.session.Session;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MagicTest {

    @Test
    void magicFunctionStrip() {
        Session session = new Session();
        session.putMagic("host", "example.com:8080");

        String test = Magic.magicReplace("https://{strip(:8080)$host}", session, false);

        assertEquals("https://example.com", test);
    }

    @Test
    void magicFunctionRepalce() {
        Session session = new Session();
        session.putMagic("host", "example.com:8080");

        String test = Magic.magicReplace("https://{replace(:8080|)$host}", session, false);

        assertEquals("https://example.com", test);
    }
}
package com.mimecast.robin.smtp.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DigestDataTest {

    @Test
    void all() {
        DigestData digestData = new DigestData();

        digestData.setHost("example.com");
        digestData.setUsername("tony@example.com");
        digestData.setRealm("example.net");

        String nonce = new SecureRandom().generate(16);
        digestData.setNonce(nonce);

        String cnonce = new SecureRandom().generate(32);
        digestData.setCnonce(cnonce);

        assertEquals("example.com", digestData.getHost());
        assertEquals("tony@example.com", digestData.getUsername());
        assertEquals("example.net", digestData.getRealm());
        assertEquals(nonce, digestData.getNonce());
        assertEquals(cnonce, digestData.getCnonce());
    }

    @Test
    void none() {
        DigestData digestData = new DigestData();
        assertEquals("", digestData.getHost());
        assertEquals("", digestData.getUsername());
        assertEquals("", digestData.getRealm());
        assertEquals("", digestData.getNonce());
        assertEquals("", digestData.getCnonce());
    }
}

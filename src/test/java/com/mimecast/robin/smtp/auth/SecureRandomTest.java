package com.mimecast.robin.smtp.auth;

import org.apache.geronimo.mail.util.Hex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecureRandomTest {

    @Test
    void generate() {
        Random random = new SecureRandom();
        String base64 = random.generate(42);
        byte[] bytes = Hex.decode(base64.getBytes());
        assertEquals(42, bytes.length);
    }
}

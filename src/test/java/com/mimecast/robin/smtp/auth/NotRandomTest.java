package com.mimecast.robin.smtp.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotRandomTest {

    @Test
    void generate() {
        Random random = new NotRandom("whatever");
        assertEquals("whatever", random.generate(1));
    }
}

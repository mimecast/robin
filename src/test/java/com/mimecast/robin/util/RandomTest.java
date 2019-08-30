package com.mimecast.robin.util;

import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void chFixedLength() {
        assertEquals(20, Random.ch().length());
    }

    @Test
    void chVariableLength() {
        assertEquals(99, Random.ch(99).length());
    }

    @Test
    void noFixedLength() {
        assertTrue(Random.no() <= 10);
    }

    @Test
    void noVariableLength() {
        assertTrue(Random.no(20) <= 20);
    }
}

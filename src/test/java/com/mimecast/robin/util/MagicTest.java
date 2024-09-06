package com.mimecast.robin.util;

import com.mimecast.robin.smtp.session.Session;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MagicTest {

    @BeforeAll
    static void before() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void magicReplace() {
        Session session = new Session();

        session.putMagic("port", "25");
        assertEquals("25", Magic.magicReplace("{$port}", session, false));

        session.putMagic("hostnames", List.of("example.com"));
        assertEquals("example.com", Magic.magicReplace("{$hostnames[0]}", session, false));

        session.saveResults("hostnames", List.of("example.com"));
        assertNotNull(Magic.magicReplace("{$hostnames[?]}", session, false));

        session.saveResults("host", List.of(Map.of("com", "example.com")));
        assertEquals("example.com", Magic.magicReplace("{$host[0][com]}", session, false));

        int offset = TimeZone.getDefault().getRawOffset();

        session.putMagic("date", "20240109000000000");
        assertEquals(String.valueOf(1704758400000L - offset), Magic.magicReplace("{dateToMillis$date}", session, false));

        session.putMagic("milis", String.valueOf(1704758400000L - offset));
        assertEquals("20240109000000000", Magic.magicReplace("{millisToDate$milis}", session, false));

        session.putMagic("upper", "ABC");
        assertEquals("abc", Magic.magicReplace("{toLowerCase$upper}", session, false));

        session.putMagic("lower", "def");
        assertEquals("DEF", Magic.magicReplace("{toUpperCase$lower}", session, false));

        session.putMagic("pattern", ".*");
        assertEquals("\\Q.*\\E", Magic.magicReplace("{patternQuote$pattern}", session, false));

        session.putMagic("host", "example.com:8080");
        assertEquals("https://example.com", Magic.magicReplace("https://{strip(:8080)$host}", session, false));

        session.putMagic("host", "example.com:8080");
        assertEquals("https://example.com", Magic.magicReplace("https://{replace(:8080|)$host}", session, false));
    }
}
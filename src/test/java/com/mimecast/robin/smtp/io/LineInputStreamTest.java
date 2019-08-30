package com.mimecast.robin.smtp.io;

import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LineInputStreamTest {

    private static LineInputStream stream;

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void readLine() throws IOException {
        Map<Integer, String> lines = new HashMap<>();

        byte[] bytes;
        LineInputStream stream = new LineInputStream(new FileInputStream(new File("src/test/resources/lipsum.mixed.eol.eml")));
        while ((bytes = stream.readLine()) != null) {
            lines.put(stream.getLineNumber(), new String(bytes));
        }

        assertEquals(76, lines.size());
        assertEquals("From: <{$MAILFROM}>", lines.get(2).trim());
        assertEquals("To: <{$RCPTTO}>", lines.get(3).trim());
        assertEquals("Subject: Lipsum", lines.get(6).trim());
        assertEquals("Integer at finibus orci.", lines.get(27).trim());
        assertEquals("Content-Transfer-Encoding: 8bit", lines.get(42).trim());
        assertEquals("--MCBoundary11505141140170031--", lines.get(76).trim());
    }
}

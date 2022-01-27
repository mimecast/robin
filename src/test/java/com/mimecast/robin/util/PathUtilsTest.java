package com.mimecast.robin.util;

import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class PathUtilsTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void isFile() {
        assertTrue(PathUtils.isFile("src/test/resources/properties.json5"));
    }

    @Test
    void isNotFile() {
        assertFalse(PathUtils.isFile("src/test/resources/not.file"));
    }

    @Test
    void cleanFilePath() {
        String random = "¬!\"£$%^&*()_+Q{}:@~|<>?`-=[];'#\\,./";
        assertEquals("¬!\"£$%^&*()_+Q{}:@~|<>?`-=[];'#,.", PathUtils.normalize(random));
    }

    @Test
    void makePath() {
        String path = "/tmp/" + System.nanoTime();
        assertTrue(PathUtils.makePath(path));
        assertTrue(new File(path).delete());
    }

    @Test
    void isDirectory() {
        assertTrue(PathUtils.isDirectory("src/test/resources/"));
    }

    @Test
    void isNotDirectory() {
        assertFalse(PathUtils.isFile("src/test/resources/not.dir/"));
    }

    @Test
    void readFile() throws IOException {
        String payload = PathUtils.readFile("src/test/resources/properties.json5", Charset.defaultCharset());
        assertEquals(123, payload.charAt(0));
        assertEquals(125, payload.charAt(payload.length() - 1));
    }
}

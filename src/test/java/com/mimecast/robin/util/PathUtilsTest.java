package com.mimecast.robin.util;

import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
class PathUtilsTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void validatePath() throws IOException {
        assertEquals("src/test/resources/properties.json", PathUtils.validatePath("src/test/resources/properties.json", "Not there"));
    }

    @Test
    void validatePathException() {
        assertThrows(IOException.class, () -> PathUtils.validatePath("src/test/resources/nofile.json", "Not there"));
    }

    @Test
    void isFile() {
        assertTrue(PathUtils.isFile("src/test/resources/properties.json"));
    }

    @Test
    void isNotFile() {
        assertFalse(PathUtils.isFile("src/test/resources/not.file"));
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
        String payload = PathUtils.readFile("src/test/resources/properties.json", Charset.defaultCharset());
        assertEquals(123, payload.charAt(0));
        assertEquals(125, payload.charAt(payload.length() - 1));
    }
}

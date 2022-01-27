package com.mimecast.robin;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {

    @Test
    void noArgs() {
        List<String> logs = MainMock.main(new ArrayList<>());

        assertEquals(Main.USAGE, logs.get(0));
        assertEquals(" " + Main.DESCRIPTION, logs.get(1));
        assertEquals("", logs.get(2));
        assertEquals("usage:   [--client] [--server]\n" +
                "    --client   Run as client\n" +
                "    --server   Run as server\n", logs.get(3));
        assertEquals("", logs.get(4));
    }

    @Test
    void badArgs() {
        List<String> logs = MainMock.main(Arrays.asList("-b", "bad"));

        assertEquals(Main.USAGE, logs.get(0));
        assertEquals(" " + Main.DESCRIPTION, logs.get(1));
        assertEquals("", logs.get(2));
        assertEquals("usage:   [--client] [--server]\n" +
                "    --client   Run as client\n" +
                "    --server   Run as server\n", logs.get(3));
        assertEquals("", logs.get(4));
    }

    @Test
    void client() {
        List<String> logs = MainMock.main(Collections.singletonList("--client"));

        assertEquals("Config error: A file or a JSON are required", logs.get(0));
        assertEquals(7, logs.size());

        logs = MainMock.main(Arrays.asList("--client", "-j"));

        assertEquals("Options error: Missing argument for option: j", logs.get(0));
        assertEquals(7, logs.size());

        logs = MainMock.main(Arrays.asList("--client", "-j", ""));

        assertEquals("Config error: A file or a JSON are required", logs.get(0));
        assertEquals(7, logs.size());

        logs = MainMock.main(Arrays.asList("--client", "-j", "fake.json5"));

        assertEquals("Config error: JSON not found", logs.get(0));
        assertEquals(7, logs.size());

        logs = MainMock.main(Arrays.asList("--client", "-j", "src/test/resources/case.json5"));

        assertEquals("Config error: Config directory not found", logs.get(0));
        assertEquals(7, logs.size());

        logs = MainMock.main(Arrays.asList("--client", "-f", "src/test/resources/lipsum.eml"));

        assertEquals("Config error: MX required in file mode", logs.get(0));
        assertEquals(7, logs.size());

        logs = MainMock.main(Arrays.asList("--client", "-f", "src/test/resources/lipsum.eml", "-x", "example.com"));

        assertEquals("Config error: MAIL required in file mode", logs.get(0));
        assertEquals(7, logs.size());

        logs = MainMock.main(Arrays.asList("--client", "-f", "src/test/resources/lipsum.eml", "-x", "example.com", "-m", "john@example.com"));

        assertEquals("Config error: RCPT required in file mode", logs.get(0));
        assertEquals(7, logs.size());

        logs = MainMock.main(Arrays.asList("--client", "-f", "src/test/resources/lipsum.eml", "-x", "example.com", "-m", "john@example.com", "-r", "jane@example.com"));

        assertEquals("Config error: Config directory not found", logs.get(0));
        assertEquals(7, logs.size());
    }

    @Test
    void server() {
        List<String> logs = MainMock.main(Collections.singletonList("--server"));

        assertEquals("java -jar robin.jar --server", logs.get(0));
        assertEquals(" Debug MTA server", logs.get(1));
        assertEquals("", logs.get(2));
        assertEquals("usage:", logs.get(3));
        assertEquals(" Path to configuration directory", logs.get(4));
        assertEquals("", logs.get(5));
        assertEquals("example:", logs.get(6));
        assertEquals(" java -jar robin.jar --server config/", logs.get(7));
        assertEquals(8, logs.size());
    }
}

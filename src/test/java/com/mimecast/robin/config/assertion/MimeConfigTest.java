package com.mimecast.robin.config.assertion;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MimeConfigTest {
    static final Map<String, Object> map = Stream.of(
            new AbstractMap.SimpleEntry<>("headers",
                    Arrays.asList(
                            Arrays.asList("Subject", "Robin wrote"),
                            Arrays.asList("To", "Sir Robin <sir@example.com>"),
                            Arrays.asList("From", "Lady Robin <lady@example.com>")
                    )
            ),
            new AbstractMap.SimpleEntry<>("parts",
                    Collections.singletonList(
                            Stream.of(
                                    new AbstractMap.SimpleEntry<>("headers",
                                            Arrays.asList(
                                                    Arrays.asList("Content-Type", "text/plain; charset=\"UTF-8\""),
                                                    Arrays.asList("Content-Transfer-Encoding", "quoted-printable")
                                            )
                                    ),
                                    new AbstractMap.SimpleEntry<>("message", "Read this article!")
                            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                    )
            )
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    static final MimeConfig config = new MimeConfig(map);

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    void run() throws IOException {
        assertEquals(3, config.getHeaders().size());
        assertEquals("Robin wrote", config.getHeaders().get(0).getValue());
        assertEquals("Sir Robin <sir@example.com>", config.getHeaders().get(1).getValue());
        assertEquals("Lady Robin <lady@example.com>", config.getHeaders().get(2).getValue());

        assertEquals(1, config.getParts().size());
        assertEquals(2, config.getParts().get(0).getHeaders().size());
        assertEquals("text/plain; charset=\"UTF-8\"", config.getParts().get(0).getHeaders().get("Content-Type").get().getValue());
        assertEquals("quoted-printable", config.getParts().get(0).getHeaders().get("Content-Transfer-Encoding").get().getValue());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        config.getParts().get(0).writeTo(baos);

        assertEquals("Content-Type: text/plain; charset=\"UTF-8\"\r\n" +
                "Content-Transfer-Encoding: quoted-printable\r\n" +
                "\r\n" +
                "Read this article!\r\n", baos.toString());
    }
}
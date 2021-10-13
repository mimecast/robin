package com.mimecast.robin.mime.parts;

import com.mimecast.robin.config.assertion.MimeConfig;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.util.StreamUtils;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfMimePartTest {

    @Test
    void writeTo() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Map<String, Object> config = Stream.of(
                new AbstractMap.SimpleEntry<>("headers",
                        Arrays.asList(
                                Arrays.asList("Content-Type", "application/pdf; name=\"article.pdf\""),
                                Arrays.asList("Content-Disposition", "attachment; filename=\"article.pdf\""),
                                Arrays.asList("Content-Transfer-Encoding", "base64")
                        )
                ),
                new AbstractMap.SimpleEntry<>("folder", "src/test/resources/mime/")
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        new PdfMimePart(new MimeConfig(config), new MessageEnvelope()).writeTo(outputStream);

        Map<Integer, String> lines = StreamUtils.parseLines(outputStream);

        assertEquals("Content-Type: application/pdf; name=\"article.pdf\"\r\n", lines.get(1));
        assertEquals("Content-Disposition: attachment; filename=\"article.pdf\"\r\n", lines.get(2));
        assertEquals("Content-Transfer-Encoding: base64\r\n", lines.get(3));
        assertEquals("\r\n", lines.get(4));
        assertTrue(Base64.isBase64(lines.get(5)));
        assertTrue(Base64.isBase64(lines.get(6)));
    }
}

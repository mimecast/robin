package com.mimecast.robin.smtp.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("StatementWithEmptyBody")
class SlowInputStreamTest {

    @Test
    void fast() throws IOException {
        String stringBuilder = "MIME-Version: 1.0\r\n" +
                "From: <tony@example.com>\r\n" +
                "To: <pepper@example.com>\r\n" +
                "Subject: Lost in space\r\n" +
                "Message-ID: <23szwa4xd5ec6rf7tgyh8j9um0kiol-tony@example.com>\r\n" +
                "\r\n" +
                "Rescue me!\r\n" +
                ".\r\n";
        InputStream inputStream = new ByteArrayInputStream(stringBuilder.getBytes());
        SlowInputStream slowInputStream = new SlowInputStream(inputStream, 64, 50);
        while (slowInputStream.read() != -1) {}

        assertEquals(0, slowInputStream.getTotalWait());
    }

    @Test
    void slow() throws IOException {
        String stringBuilder = "MIME-Version: 1.0\r\n" +
                "From: <tony@example.com>\r\n" +
                "To: <pepper@example.com>\r\n" +
                "Subject: Lost in space\r\n" +
                "Message-ID: <23szwa4xd5ec6rf7tgyh8j9um0kiol-tony@example.com>\r\n" +
                "\r\n" +
                "Rescue me!\r\n" +
                ".\r\n";
        InputStream inputStream = new ByteArrayInputStream(stringBuilder.getBytes());
        SlowInputStream slowInputStream = new SlowInputStream(inputStream, 128, 100);
        while (slowInputStream.read() != -1) {}

        assertEquals(100, slowInputStream.getTotalWait());
    }
}

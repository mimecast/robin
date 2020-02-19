package com.mimecast.robin.smtp.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlowOutputStreamTest {

    @Test
    void fast() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        SlowOutputStream slowOutputStream = new SlowOutputStream(byteArrayOutputStream, 64, 50);

        String stringBuilder = "MIME-Version: 1.0\r\n" +
                "From: <tony@example.com>\r\n" +
                "To: <pepper@example.com>\r\n" +
                "Subject: Lost in space\r\n" +
                "Message-ID: <23szwa4xd5ec6rf7tgyh8j9um0kiol-tony@example.com>\r\n" +
                "\r\n" +
                "Rescue me!\r\n" +
                ".\r\n";
        InputStream inputStream = new ByteArrayInputStream(stringBuilder.getBytes());
        int intByte;
        while ((intByte = inputStream.read()) != -1) {
            slowOutputStream.write(intByte);
        }

        assertEquals(0, slowOutputStream.getTotalWait());
        assertEquals(175, byteArrayOutputStream.toString().length());
    }

    @Test
    void slow() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        SlowOutputStream slowOutputStream = new SlowOutputStream(byteArrayOutputStream, 128, 100);

        String stringBuilder = "MIME-Version: 1.0\r\n" +
                "From: <tony@example.com>\r\n" +
                "To: <pepper@example.com>\r\n" +
                "Subject: Lost in space\r\n" +
                "Message-ID: <23szwa4xd5ec6rf7tgyh8j9um0kiol-tony@example.com>\r\n" +
                "\r\n" +
                "Rescue me!\r\n" +
                ".\r\n";
        InputStream inputStream = new ByteArrayInputStream(stringBuilder.getBytes());
        int intByte;
        while ((intByte = inputStream.read()) != -1) {
            slowOutputStream.write(intByte);
        }

        assertEquals(100, slowOutputStream.getTotalWait());
        assertEquals(175, byteArrayOutputStream.toString().length());
    }
}

package com.mimecast.robin.smtp.extension.server;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import com.mimecast.robin.smtp.verb.Verb;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerDataTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void processAscii() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("MIME-Version: 1.0\r\n");
        stringBuilder.append("From: <tony@example.com>\r\n");
        stringBuilder.append("To: <pepper@example.com>\r\n");
        stringBuilder.append("Subject: Lost in space\r\n");
        stringBuilder.append("\r\n");
        stringBuilder.append("Rescue me!\r\n");
        stringBuilder.append(".\r\n\r\n\r\n");

        ConnectionMock connection = new ConnectionMock(stringBuilder);
        connection.setSocket(new Socket());

        Verb verb = new Verb("DATA");

        ServerData data = new ServerData();
        boolean process = data.process(connection, verb);

        assertTrue(process);

        connection.parseLines();
        assertEquals("354 Ready and willing\r\n", connection.getLine(1));
        assertEquals("250 2.0.0 Received OK\r\n", connection.getLine(2));
        assertEquals(stringBuilder.toString().length() - (5 + 4), data.getBytesReceived());
    }

    @Test
    void processAsciiLF() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("MIME-Version: 1.0\n");
        stringBuilder.append("From: <tony@example.com>\n");
        stringBuilder.append("To: <pepper@example.com>\n");
        stringBuilder.append("Subject: Lost in space\n");
        stringBuilder.append("\n");
        stringBuilder.append("Rescue me!\n");
        stringBuilder.append(".\n\n\n");

        ConnectionMock connection = new ConnectionMock(stringBuilder);
        connection.setSocket(new Socket());

        Verb verb = new Verb("DATA");

        ServerData data = new ServerData();
        boolean process = data.process(connection, verb);

        assertTrue(process);

        connection.parseLines();
        assertEquals("354 Ready and willing\r\n", connection.getLine(1));
        assertEquals("250 2.0.0 Received OK\r\n", connection.getLine(2));
        assertEquals(stringBuilder.toString().length() - (3 + 2), data.getBytesReceived());
    }

    @Test
    void processAsciiCR() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("MIME-Version: 1.0\r");
        stringBuilder.append("From: <tony@example.com>\r");
        stringBuilder.append("To: <pepper@example.com>\r");
        stringBuilder.append("Subject: Lost in space\r");
        stringBuilder.append("\r");
        stringBuilder.append("Rescue me!\r");
        stringBuilder.append(".\r\r\r");

        ConnectionMock connection = new ConnectionMock(stringBuilder);
        connection.setSocket(new Socket());

        Verb verb = new Verb("DATA");

        ServerData data = new ServerData();
        boolean process = data.process(connection, verb);

        assertTrue(process);

        connection.parseLines();
        assertEquals("354 Ready and willing\r\n", connection.getLine(1));
        assertEquals("250 2.0.0 Received OK\r\n", connection.getLine(2));
        assertEquals(stringBuilder.toString().length() - (3 + 2), data.getBytesReceived());
    }

    @Test
    void processBinary() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("MIME-Version: 1.0\r\n");
        stringBuilder.append("From: <tony@example.com>\r\n");
        stringBuilder.append("To: <pepper@example.com>\r\n");
        stringBuilder.append("Subject: Lost in space\r\n");
        stringBuilder.append("\r\n");
        stringBuilder.append("Rescue me!\r\n\r\n");

        ConnectionMock connection = new ConnectionMock(stringBuilder);
        connection.setSocket(new Socket());

        Verb verb = new Verb("BDAT 109 LAST");

        ServerData data = new ServerData();
        boolean process = data.process(connection, verb);

        assertTrue(process);

        connection.parseLines();
        assertEquals("250 2.0.0 Chunk OK\r\n", connection.getLine(1));
        assertEquals(stringBuilder.toString().length() - 2, data.getBytesReceived());
    }
}

package com.mimecast.robin.smtp;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailReceiptTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    private ConnectionMock getConnection(StringBuilder stringBuilder) {
        ConnectionMock connection = new ConnectionMock(stringBuilder);
        connection.getSession().setRdns("example.com");
        connection.getSession().setFriendRdns("example.net");
        connection.getSession().setFriendAddr("127.0.0.1");

        return connection;
    }

    @Test
    void helo() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("EHLO helo.com\r\n");
        stringBuilder.append("HELO example.com\r\n");
        stringBuilder.append("QUIT\r\n");

        ConnectionMock connection = getConnection(stringBuilder);
        new EmailReceipt(connection).run();

        connection.parseLines();
        assertEquals("500 ESMTP Error (Try again using SMTP)\r\n", connection.getLine(2));
        assertEquals("250 Welcome [example.net (127.0.0.1)]\r\n", connection.getLine(3));
        assertEquals("221 2.0.0 Closing connection\r\n", connection.getLine(4));
    }

    @Test
    void receive() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("EHLO example.com\r\n");
        stringBuilder.append("MAIL FROM: <john@example.com>\r\n");
        stringBuilder.append("RCPT TO: <jane@example.com>\r\n");
        stringBuilder.append("DATA\r\n");
        stringBuilder.append("MIME-Version: 1.0\r\n" +
                "From: <tony@example.com>\r\n" +
                "To: <pepper@example.com>\r\n" +
                "Subject: Lost in space\r\n" +
                "Message-ID: <23szwa4xd5ec6rf7tgyh8j9um0kiol-tony@example.com>\r\n" +
                "\r\n" +
                "Rescue me!\r\n" +
                ".\r\n");
        stringBuilder.append("QUIT\r\n");

        ConnectionMock connection = getConnection(stringBuilder);
        new EmailReceipt(connection).run();

        connection.parseLines();
        assertEquals("250-Welcome [example.net (127.0.0.1)]\r\n", connection.getLine(2));
        assertEquals("250-SMTPUTF8\r\n", connection.getLine(3));
        assertEquals("250-HELP\r\n", connection.getLine(4));
        assertEquals("250-PIPELINING\r\n", connection.getLine(5));
        assertEquals("250-AUTH PLAIN LOGIN\r\n", connection.getLine(6));
        assertEquals("250-STARTTLS\r\n", connection.getLine(7));
        assertEquals("250 CHUNKING\r\n", connection.getLine(8));
        assertTrue(connection.getLine(9).startsWith("250 2.1.0 Sender OK"), "startsWith(\"250 2.1.0 Sender OK\")");
        assertTrue(connection.getLine(10).startsWith("250 2.1.5 Recipient OK"), "startsWith(\"250 2.1.5 Recipient OK\")");
        assertEquals("354 Ready and willing\r\n", connection.getLine(11));
        assertTrue(connection.getLine(12).startsWith("250 2.0.0 Received OK"), "startsWith(\"250 2.0.0 Received OK\")");
        assertEquals("221 2.0.0 Closing connection\r\n", connection.getLine(13));
    }

    @Test
    void reject() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("EHLO rejectmail.com\r\n");
        stringBuilder.append("MAIL FROM: <john@example.com>\r\n");
        stringBuilder.append("RCPT TO: <jane@example.com>\r\n");
        stringBuilder.append("QUIT\r\n");

        ConnectionMock connection = getConnection(stringBuilder);
        new EmailReceipt(connection).run();

        connection.parseLines();
        assertEquals("250-Welcome [example.net (127.0.0.1)]\r\n", connection.getLine(2));
        assertEquals("250-SMTPUTF8\r\n", connection.getLine(3));
        assertEquals("250-HELP\r\n", connection.getLine(4));
        assertEquals("250-PIPELINING\r\n", connection.getLine(5));
        assertEquals("250-AUTH PLAIN LOGIN\r\n", connection.getLine(6));
        assertEquals("250-STARTTLS\r\n", connection.getLine(7));
        assertEquals("250 CHUNKING\r\n", connection.getLine(8));
        assertTrue(connection.getLine(9).startsWith("250 2.1.0 Sender OK"), "startsWith(\"250 2.1.0 Sender OK\")");
        assertEquals("501 Invalid address\r\n", connection.getLine(10));
        assertEquals("221 2.0.0 Closing connection\r\n", connection.getLine(11));
    }

    @Test
    void rejectPipelining() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("EHLO rejectmail.com\r\n");
        stringBuilder.append("MAIL FROM: <john@example.com>\r\n");
        stringBuilder.append("RCPT TO: <jane@example.com>\r\n");
        stringBuilder.append("DATA\r\n");
        stringBuilder.append("QUIT\r\n");

        ConnectionMock connection = getConnection(stringBuilder);
        new EmailReceipt(connection).run();

        connection.parseLines();
        assertEquals("250-Welcome [example.net (127.0.0.1)]\r\n", connection.getLine(2));
        assertEquals("250-SMTPUTF8\r\n", connection.getLine(3));
        assertEquals("250-HELP\r\n", connection.getLine(4));
        assertEquals("250-PIPELINING\r\n", connection.getLine(5));
        assertEquals("250-AUTH PLAIN LOGIN\r\n", connection.getLine(6));
        assertEquals("250-STARTTLS\r\n", connection.getLine(7));
        assertEquals("250 CHUNKING\r\n", connection.getLine(8));
        assertTrue(connection.getLine(9).startsWith("250 2.1.0 Sender OK"), "startsWith(\"250 2.1.0 Sender OK\")");
        assertEquals("501 Invalid address\r\n", connection.getLine(10));
        assertTrue(connection.getLine(11).startsWith("554 5.5.1 No valid recipients"), "startsWith(\"554 5.5.1 No valid recipients\")");
        assertEquals("221 2.0.0 Closing connection\r\n", connection.getLine(12));
    }
}

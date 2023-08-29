package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientEhloTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void process() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("250-smtp.example.com at your service, [127.0.0.1]\r\n" +
                "250 STARTTLS\r\n");
        ConnectionMock connection = new ConnectionMock(stringBuilder);

        ClientEhlo ehlo = new ClientEhlo();
        boolean process = ehlo.process(connection);

        assertTrue(process);
        assertTrue(connection.getSession().getSessionTransactionList().getLast("EHLO").getPayload().startsWith("EHLO "));

    }

    @Test
    void processSession() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("250-smtp.example.com at your service, [127.0.0.1]\r\n" +
                "250-SIZE 35882577\r\n" +
                "250-STARTTLS\r\n" +
                "250-AUTH LOGIN PLAIN NTLM CRAM-MD5 DIGEST-MD5 XOAUTH XOAUTH2\r\n" +
                "250-ENHANCEDSTATUSCODES\r\n" +
                "250-PIPELINING\r\n" +
                "250-CHUNKING\r\n" +
                "250-8BITMIME\r\n" +
                "250-BINARYMIME\r\n" +
                "250 SMTPUTF8\r\n");
        ConnectionMock connection = new ConnectionMock(stringBuilder);
        connection.getSession().setEhlo("example.com");

        ClientEhlo ehlo = new ClientEhlo();
        boolean process = ehlo.process(connection);

        assertTrue(process);
        assertEquals(35882577, connection.getSession().getEhloSize());
        assertEquals("EHLO example.com", connection.getSession().getSessionTransactionList().getLast("EHLO").getPayload());
        assertTrue(connection.getSession().getEhloAuth().contains("login"));
        assertTrue(connection.getSession().getEhloAuth().contains("plain"));
        assertTrue(connection.getSession().getEhloAuth().contains("ntlm"));
        assertTrue(connection.getSession().getEhloAuth().contains("cram-md5"));
        assertTrue(connection.getSession().getEhloAuth().contains("digest-md5"));
        assertTrue(connection.getSession().getEhloAuth().contains("xoauth"));
        assertTrue(connection.getSession().getEhloAuth().contains("xoauth2"));
        assertTrue(connection.getSession().isEhlo8bit());
        assertTrue(connection.getSession().isEhloBinary());
        assertTrue(connection.getSession().isEhloBdat());
        assertTrue(connection.getSession().isEhloTls());

    }
}

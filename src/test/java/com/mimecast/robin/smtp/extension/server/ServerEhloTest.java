package com.mimecast.robin.smtp.extension.server;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import com.mimecast.robin.smtp.verb.Verb;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerEhloTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void process() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        ConnectionMock connection = new ConnectionMock(stringBuilder);
        connection.getSession().setFriendRdns("example.com");
        connection.getSession().setFriendAddr("127.0.0.1");

        Verb verb = new Verb("EHLO example.com");

        ServerEhlo ehlo = new ServerEhlo();
        boolean process = ehlo.process(connection, verb);

        assertTrue(process);
        assertEquals("example.com", connection.getSession().getEhlo());

        String out = connection.getOutput();
        assertTrue(out.contains("example.com"));
        assertTrue(out.contains("127.0.0.1"));
        assertTrue(out.contains("HELP"));
        assertTrue(out.contains("STARTTLS"));
        assertTrue(out.contains("AUTH"));
        assertTrue(out.contains("PLAIN"));
        assertTrue(out.contains("LOGIN"));
    }
}

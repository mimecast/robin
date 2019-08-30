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

class ServerXclientTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void process() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        ConnectionMock connection = new ConnectionMock(stringBuilder);
        connection.getSession().setRdns("example.com");

        Verb verb = new Verb("XCLIENT helo=example.com name=example.com addr=127.0.0.1");

        ServerXclient xclient = new ServerXclient();
        boolean process = xclient.process(connection, verb);

        assertTrue(process);

        connection.parseLines();
        assertTrue(connection.getLine(1).startsWith("220 example.com ESMTP"));
        assertEquals("example.com", connection.getSession().getEhlo());
        assertEquals("example.com", connection.getSession().getFriendRdns());
        assertEquals("127.0.0.1", connection.getSession().getFriendAddr());
    }
}

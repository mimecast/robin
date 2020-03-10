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

class ServerRcptTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void processWithScenario() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        ConnectionMock connection = new ConnectionMock(stringBuilder);

        Verb verb = new Verb("RCPT TO: <friday-123@example.com>");

        ServerRcpt rcpt = new ServerRcpt();
        boolean process = rcpt.process(connection, verb);

        assertTrue(process);
        assertEquals("friday-123@example.com", rcpt.getAddress().getAddress());
        assertEquals("friday-123@example.com", connection.getSession().getRcpts().get(0).getAddress());
    }
}

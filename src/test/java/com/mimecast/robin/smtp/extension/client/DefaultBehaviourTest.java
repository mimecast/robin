package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultBehaviourTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void process() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("250-smtp.example.com at your service, [127.0.0.1]\r\n" +
                "250 HELP\r\n");
        stringBuilder.append("235 2.7.0 Authorized\r\n");
        stringBuilder.append("250 2.1.0 Sender OK\r\n");
        stringBuilder.append("250 2.1.5 Recipient OK\r\n");
        stringBuilder.append("354 Ready and willing\r\n");
        stringBuilder.append("250 2.0.0 Received OK\r\n");
        stringBuilder.append("221 2.0.0 Closing connection\r\n");

        ConnectionMock connection = new ConnectionMock(stringBuilder);
        connection.getSession().setEhlo("example.com");
        connection.getSession().setAuth(true);
        connection.getSession().setUsername("tony@example.com");
        connection.getSession().setPassword("giveHerTheRing");
        connection.getSession().setEhloAuth(Collections.singletonList("plain"));

        MessageEnvelope envelope = new MessageEnvelope();
        envelope.setMail("tony@example.com");
        envelope.setRcpt("pepper@example.com");
        envelope.setSubject("Lost in space");
        envelope.setMessage("Rescue me!");
        connection.getSession().addEnvelope(envelope);

        DefaultBehaviour behaviour = new DefaultBehaviour();
        behaviour.process(connection);

        connection.parseLines();
        assertEquals("EHLO example.com\r\n", connection.getLine(1));
        assertEquals("AUTH PLAIN dG9ueUBleGFtcGxlLmNvbQB0b255QGV4YW1wbGUuY29tAGdpdmVIZXJUaGVSaW5n\r\n", connection.getLine(2));
        assertEquals("MAIL FROM:<tony@example.com> SIZE=294\r\n", connection.getLine(3));
        assertEquals("RCPT TO:<pepper@example.com>\r\n", connection.getLine(4));
        assertEquals("DATA\r\n", connection.getLine(5));
        assertEquals("MIME-Version: 1.0\r\n", connection.getLine(6));
        assertEquals("Subject: Lost in space\r\n", connection.getLine(11));
        assertEquals("\r\n", connection.getLine(14));
        assertEquals("Rescue me!\r\n", connection.getLine(15));
        assertEquals(".\r\n", connection.getLine(16));
        assertEquals("QUIT\r\n", connection.getLine(17));
    }
}

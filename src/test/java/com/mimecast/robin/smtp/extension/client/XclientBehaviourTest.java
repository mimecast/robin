package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import com.mimecast.robin.smtp.session.XclientSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XclientBehaviourTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void process() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("250-smtp.example.com at your service, [127.0.0.1]\r\n" +
                "250 HELP\r\n");
        stringBuilder.append("220 example.com ESMTP\r\n");
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

        Map<String, String> map = new HashMap<>();
        map.put("name", "example.com");
        map.put("helo", "example.com");
        map.put("addr", "127.0.0.1");
        ((XclientSession) connection.getSession()).setXclient(map);

        MessageEnvelope envelope = new MessageEnvelope();
        envelope.setMail("tony@example.com");
        envelope.setRcpt("pepper@example.com");
        envelope.setSubject("Lost in space");
        envelope.setMessage("Rescue me!");
        connection.getSession().addEnvelope(envelope);

        XclientBehaviour behaviour = new XclientBehaviour();
        behaviour.process(connection);

        connection.parseLines();
        assertEquals("EHLO example.com\r\n", connection.getLine(1));
        assertEquals("XCLIENT helo=example.com name=example.com addr=127.0.0.1\r\n", connection.getLine(2));
        assertEquals("EHLO example.com\r\n", connection.getLine(3));
        assertEquals("AUTH PLAIN dG9ueUBleGFtcGxlLmNvbQB0b255QGV4YW1wbGUuY29tAGdpdmVIZXJUaGVSaW5n\r\n", connection.getLine(4));
        assertEquals("MAIL FROM:<tony@example.com> SIZE=294\r\n", connection.getLine(5));
        assertEquals("RCPT TO:<pepper@example.com>\r\n", connection.getLine(6));
        assertEquals("DATA\r\n", connection.getLine(7));
        assertEquals("MIME-Version: 1.0\r\n", connection.getLine(8));
        assertEquals("Subject: Lost in space\r\n", connection.getLine(13));
        assertEquals("\r\n", connection.getLine(16));
        assertEquals("Rescue me!\r\n", connection.getLine(17));
        assertEquals(".\r\n", connection.getLine(18));
        assertEquals("QUIT\r\n", connection.getLine(19));
    }
}

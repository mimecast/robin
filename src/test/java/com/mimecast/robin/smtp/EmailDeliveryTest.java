package com.mimecast.robin.smtp;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class EmailDeliveryTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    private ConnectionMock getConnection(StringBuilder stringBuilder) {
        ConnectionMock connection = new ConnectionMock(stringBuilder);
        connection.getSession().setEhlo("example.com");
        connection.getSession().setMx(Collections.singletonList("example.com"));
        connection.getSession().setPort(25);

        MessageEnvelope envelope = new MessageEnvelope();
        envelope.setMail("tony@example.com");
        envelope.setRcpt("pepper@example.com");
        envelope.setSubject("Lost in space");
        envelope.setMessage("Rescue me!");
        connection.getSession().addEnvelope(envelope);

        return connection;
    }

    @Test
    void processAscii() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("220 example.com ESMTP\r\n");
        stringBuilder.append("250-smtp.example.com at your service, [127.0.0.1]\r\n" +
                "250 HELP\r\n");
        stringBuilder.append("250 2.1.0 Sender OK\r\n");
        stringBuilder.append("250 2.1.5 Recipient OK\r\n");
        stringBuilder.append("354 Ready and willing\r\n");
        stringBuilder.append("250 2.0.0 Received OK\r\n");
        stringBuilder.append("221 2.0.0 Closing connection\r\n");

        ConnectionMock connection = getConnection(stringBuilder);
        new EmailDeliveryMock(connection).send();

        connection.parseLines();
        assertEquals("EHLO example.com\r\n", connection.getLine(1));
        assertEquals("MAIL FROM:<tony@example.com> SIZE=294\r\n", connection.getLine(2));
        assertEquals("RCPT TO:<pepper@example.com>\r\n", connection.getLine(3));
        assertEquals("DATA\r\n", connection.getLine(4));
        assertEquals("MIME-Version: 1.0\r\n", connection.getLine(5));
        assertEquals("From: <tony@example.com>\r\n", connection.getLine(8));
        assertEquals("To: <pepper@example.com>\r\n", connection.getLine(9));
        assertEquals("Subject: Lost in space\r\n", connection.getLine(10));
        assertEquals("Content-Type: text/plain\r\n", connection.getLine(11));
        assertEquals("Content-Transfer-Encoding: 8bit\r\n", connection.getLine(12));
        assertEquals("\r\n", connection.getLine(13));
        assertEquals("Rescue me!\r\n", connection.getLine(14));
        assertEquals(".\r\n", connection.getLine(15));
        assertEquals("QUIT\r\n", connection.getLine(16));

        assertEquals("220 example.com ESMTP", connection.getSession().getSessionTransactionList().getLast("SMTP").getResponse());
        assertEquals("250-smtp.example.com at your service, [127.0.0.1]\r\n" +
                "250 HELP", connection.getSession().getSessionTransactionList().getLast("EHLO").getResponse());

        assertEquals("250 2.1.0 Sender OK", connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getMail().getResponse());
        assertEquals(1, connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getRcpt().size());
        assertEquals("250 2.1.5 Recipient OK", connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getRcpt().get(0).getResponse());
        assertEquals("250 2.0.0 Received OK", connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getData().getResponse());
        assertTrue(connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getBdat().isEmpty());

        assertEquals("221 2.0.0 Closing connection", connection.getSession().getSessionTransactionList().getLast("QUIT").getResponse());
    }

    @Test
    void processAsciiWithErrors() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("220 example.com ESMTP\r\n");
        stringBuilder.append("500 5.3.3 Unrecognized command\r\n");
        stringBuilder.append("221 2.0.0 Closing connection\r\n");

        ConnectionMock connection = getConnection(stringBuilder);
        new EmailDeliveryMock(connection).send();

        connection.parseLines();
        assertEquals("EHLO example.com\r\n", connection.getLine(1));

        assertEquals("220 example.com ESMTP", connection.getSession().getSessionTransactionList().getLast("SMTP").getResponse());
        assertEquals("500 5.3.3 Unrecognized command", connection.getSession().getSessionTransactionList().getLast("EHLO").getResponse());

        assertTrue(connection.getSession().getSessionTransactionList().getEnvelopes().isEmpty());
    }

    @Test
    void processAsciiWithEnvelopeErrors() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("220 example.com ESMTP\r\n");
        stringBuilder.append("250-smtp.example.com at your service, [127.0.0.1]\r\n" +
                "250 HELP\r\n");
        stringBuilder.append("250 2.1.0 Sender OK\r\n");
        stringBuilder.append("550 5.1.1 Recipient unknown\r\n");
        stringBuilder.append("221 2.0.0 Closing connection\r\n");

        ConnectionMock connection = getConnection(stringBuilder);
        new EmailDeliveryMock(connection).send();

        connection.parseLines();
        assertEquals("EHLO example.com\r\n", connection.getLine(1));
        assertEquals("MAIL FROM:<tony@example.com> SIZE=294\r\n", connection.getLine(2));
        assertEquals("RCPT TO:<pepper@example.com>\r\n", connection.getLine(3));
        assertEquals("QUIT\r\n", connection.getLine(4));

        assertEquals("220 example.com ESMTP", connection.getSession().getSessionTransactionList().getLast("SMTP").getResponse());
        assertEquals("250-smtp.example.com at your service, [127.0.0.1]\r\n" +
                "250 HELP", connection.getSession().getSessionTransactionList().getLast("EHLO").getResponse());

        assertEquals("250 2.1.0 Sender OK", connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getMail().getResponse());
        assertEquals(1, connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getRcpt().size());
        assertEquals("550 5.1.1 Recipient unknown", connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getRcpt().get(0).getResponse());
        assertNull(connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getData());
        assertTrue(connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getBdat().isEmpty());

        assertEquals("221 2.0.0 Closing connection", connection.getSession().getSessionTransactionList().getLast("QUIT").getResponse());
    }

    @Test
    void processBinary() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("220 example.com ESMTP\r\n");
        stringBuilder.append("250-smtp.example.com at your service, [127.0.0.1]\r\n" +
                "250 HELP\r\n");
        stringBuilder.append("250 2.1.0 Sender OK\r\n");
        stringBuilder.append("250 2.1.5 Recipient OK\r\n");
        stringBuilder.append("250 2.0.0 Chunk OK\r\n");
        stringBuilder.append("250 2.0.0 Chunk OK\r\n");
        stringBuilder.append("221 2.0.0 Closing connection\r\n");

        ConnectionMock connection = getConnection(stringBuilder);
        connection.getSession().setEhloBdat(true);
        connection.getSession().getEnvelopes().get(0).setChunkSize(512);

        new EmailDeliveryMock(connection).send();

        connection.parseLines();
        assertEquals("EHLO example.com\r\n", connection.getLine(1));
        assertEquals("MAIL FROM:<tony@example.com> SIZE=294\r\n", connection.getLine(2));
        assertEquals("RCPT TO:<pepper@example.com>\r\n", connection.getLine(3));
        assertEquals("BDAT 277\r\n", connection.getLine(4));
        assertEquals("MIME-Version: 1.0\r\n", connection.getLine(5));
        assertEquals("From: <tony@example.com>\r\n", connection.getLine(8));
        assertEquals("To: <pepper@example.com>\r\n", connection.getLine(9));
        assertEquals("Subject: Lost in space\r\n", connection.getLine(10));
        assertEquals("Content-Type: text/plain\r\n", connection.getLine(11));
        assertEquals("Content-Transfer-Encoding: 8bit\r\n", connection.getLine(12));
        assertEquals("\r\n", connection.getLine(13));
        assertEquals("BDAT 12 LAST\r\n", connection.getLine(14));
        assertEquals("Rescue me!\r\n", connection.getLine(15));
        assertEquals("QUIT\r\n", connection.getLine(16));

        assertEquals("220 example.com ESMTP", connection.getSession().getSessionTransactionList().getLast("SMTP").getResponse());
        assertEquals("250-smtp.example.com at your service, [127.0.0.1]\r\n" +
                "250 HELP", connection.getSession().getSessionTransactionList().getLast("EHLO").getResponse());

        assertEquals("250 2.1.0 Sender OK", connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getMail().getResponse());
        assertEquals(1, connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getRcpt().size());
        assertEquals("250 2.1.5 Recipient OK", connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getRcpt().get(0).getResponse());
        assertNull(connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getData());
        assertEquals("250 2.0.0 Chunk OK", connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getBdat().get(0).getResponse());

        assertEquals("221 2.0.0 Closing connection", connection.getSession().getSessionTransactionList().getLast("QUIT").getResponse());
    }

    @Test
    void processBinaryLong() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("220 example.com ESMTP\r\n");
        stringBuilder.append("250-smtp.example.com at your service, [127.0.0.1]\r\n" +
                "250 HELP\r\n");
        stringBuilder.append("250 2.1.0 Sender OK\r\n");
        stringBuilder.append("250 2.1.5 Recipient OK\r\n");
        stringBuilder.append("250 2.0.0 Chunk OK\r\n");
        stringBuilder.append("250 2.0.0 Chunk OK\r\n");
        stringBuilder.append("221 2.0.0 Closing connection\r\n");

        ConnectionMock connection = getConnection(stringBuilder);

        String message = StringUtils.repeat("Part of the journey is the end.\r\n", 200);
        connection.getSession().getEnvelopes().get(0).setMessage(message);
        connection.getSession().getEnvelopes().get(0).setChunkWrite(true);

        connection.getSession().setEhloBdat(true);
        connection.getSession().getEnvelopes().get(0).setChunkSize(512);

        new EmailDeliveryMock(connection).send();

        connection.parseLines();
        assertEquals("EHLO example.com\r\n", connection.getLine(1));
        assertEquals("MAIL FROM:<tony@example.com> SIZE=6884\r\n", connection.getLine(2));
        assertEquals("RCPT TO:<pepper@example.com>\r\n", connection.getLine(3));
        assertEquals("BDAT 277\r\n", connection.getLine(4));
        assertEquals("MIME-Version: 1.0\r\n", connection.getLine(5));
        assertEquals("From: <tony@example.com>\r\n", connection.getLine(8));
        assertEquals("To: <pepper@example.com>\r\n", connection.getLine(9));
        assertEquals("Subject: Lost in space\r\n", connection.getLine(10));
        assertEquals("Content-Type: text/plain\r\n", connection.getLine(11));
        assertEquals("Content-Transfer-Encoding: 8bit\r\n", connection.getLine(12));
        assertEquals("\r\n", connection.getLine(13));
        assertEquals("BDAT 6602 LAST\r\n", connection.getLine(14));
        assertEquals("Part of the journey is the end.\r\n", connection.getLine(15));
        assertEquals("Part of the journey is the end.\r\n", connection.getLine(214));
        assertEquals("QUIT\r\n", connection.getLine(216));

        assertEquals("220 example.com ESMTP", connection.getSession().getSessionTransactionList().getLast("SMTP").getResponse());
        assertEquals("250-smtp.example.com at your service, [127.0.0.1]\r\n" +
                "250 HELP", connection.getSession().getSessionTransactionList().getLast("EHLO").getResponse());

        assertEquals("250 2.1.0 Sender OK", connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getMail().getResponse());
        assertEquals(1, connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getRcpt().size());
        assertEquals("250 2.1.5 Recipient OK", connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getRcpt().get(0).getResponse());
        assertNull(connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getData());
        assertEquals("250 2.0.0 Chunk OK", connection.getSession().getSessionTransactionList().getEnvelopes().get(0).getBdat().get(0).getResponse());

        assertEquals("221 2.0.0 Closing connection", connection.getSession().getSessionTransactionList().getLast("QUIT").getResponse());
    }
}
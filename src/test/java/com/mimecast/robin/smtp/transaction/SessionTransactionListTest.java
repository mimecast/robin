package com.mimecast.robin.smtp.transaction;

import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

import static org.junit.jupiter.api.Assertions.*;

class SessionTransactionListTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void testWithEnvelope() {
        SessionTransactionList session = new SessionTransactionList();

        // SMTP
        String smtp = "220 example.com ESMTP";
        session.addTransaction("SMTP", smtp, false);

        assertEquals(smtp, session.getLast("SMTP").getResponse());
        assertNull(session.getLast("QUIT"));

        // EHLO
        String ehlo = "250 Welcome";
        session.addTransaction("EHLO", ehlo, true);

        assertEquals(ehlo, session.getLast("EHLO").getResponse());
        assertTrue(session.getLast("EHLO").isError());

        // QUIT
        String quit = "221 Bye";
        session.addTransaction("QUIT", quit);
        session.addTransaction("QUIT", quit); // Skipped

        assertEquals(quit, session.getLast("QUIT").getResponse());

        // Envelope
        EnvelopeTransactionList envelope = new EnvelopeTransactionList();
        session.addEnvelope(envelope);

        assertEquals(1, session.getEnvelopes().size());
        assertNull(session.getEnvelopes().get(0).getMail());
        assertTrue(session.getEnvelopes().get(0).getRcpt().isEmpty());
        assertNull(session.getEnvelopes().get(0).getData());
        assertTrue(session.getEnvelopes().get(0).getBdat().isEmpty());

        // MAIL
        String mailp = "MAIL FROM: <tony@example.com>";
        String mailr = "250 Sender OK [7qGJZ4oRNkWJPsu_7ug1nw.localhost]";
        envelope.addTransaction("MAIL", mailp, mailr, false);

        assertEquals(mailp, session.getEnvelopes().get(0).getMail().getPayload());
        assertEquals(mailr, session.getEnvelopes().get(0).getMail().getResponse());

        // RCPT
        String rcptp = "RCPT TO: <pepper@xample.com>";
        String rcptr = "250 Recipient OK";
        envelope.addTransaction("RCPT", rcptr, true);
        envelope.addTransaction("RCPT", rcptp, rcptr);

        assertEquals(rcptr, session.getEnvelopes().get(0).getRcpt().get(0).getResponse());
        assertEquals(rcptp, session.getEnvelopes().get(0).getRcpt().get(1).getPayload());
        assertFalse(session.getEnvelopes().get(0).getRcpt().get(1).isError());
        assertEquals(1, session.getEnvelopes().get(0).getRcptErrors().size());

        // DATA
        String data = "250 Received OK";
        envelope.addTransaction("DATA", data, false);

        assertEquals(data, session.getEnvelopes().get(0).getData().getResponse());

        // BDAT
        String bdatp = "BDAT 123 LAST";
        String bdatr = "250 OK";
        envelope.addTransaction("BDAT", bdatp, bdatr, false);

        assertEquals(bdatp, session.getEnvelopes().get(0).getBdat().get(0).getPayload());
        assertEquals(bdatr, session.getEnvelopes().get(0).getBdat().get(0).getResponse());

        // Errors
        assertEquals(1, session.getErrors().size());
    }
}

package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.transaction.EnvelopeTransactionList;

import java.io.IOException;

/**
 * RCPT extension processor.
 */
public class ClientRcpt extends ClientProcessor {

    /**
     * RCPT processor.
     *
     * @param connection Connection instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection) throws IOException {
        super.process(connection);

        // Select message and envelope to send.
        int messageID = connection.getSessionTransactionList().getEnvelopes().size() - 1; // Adjust as it's initially added in ClientMail.
        MessageEnvelope envelope = connection.getSession().getEnvelopes().get(messageID);

        // Get delivery envelope.
        EnvelopeTransactionList envelopeTransactions = connection.getSessionTransactionList().getEnvelopes().get(messageID);

        // Loop recipients.
        String write;
        String read;
        boolean accepting = false;
        for (String to : envelope.getRcpts()) {
            write = "RCPT TO:<" + to + ">";
            connection.write(write);

            read = connection.read("250");

            if (read.startsWith("250")) accepting = true;
            envelopeTransactions.addTransaction("RCPT", write, read, !accepting);
        }

        return accepting;
    }
}

package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.transaction.EnvelopeTransactionList;

import java.io.File;
import java.io.IOException;

/**
 * MAIL extension processor.
 */
public class ClientMail extends ClientProcessor {

    /**
     * MAIL processor.
     *
     * @param connection Connection instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection) throws IOException {
        super.process(connection);

        // Select message to send.
        int messageID = connection.getSessionTransactionList().getEnvelopes().size();
        MessageEnvelope envelope = connection.getSession().getEnvelopes().get(messageID);

        // Construct delivery envelope.
        EnvelopeTransactionList transactionList = new EnvelopeTransactionList();

        // Sender.
        String write = "MAIL FROM:<" + envelope.getMail() + "> SIZE=" + sizeMessage(envelope);
        connection.write(write);

        String read = connection.read("250");

        transactionList.addTransaction("MAIL", write, read, !read.startsWith("250"));

        // Add transaction list to envelope.
        connection.getSessionTransactionList().addEnvelope(transactionList);

        return read.startsWith("250");
    }

    /**
     * Get envelope size for MAIL parameter.
     *
     * @param envelope MessageEnvelope instance.
     * @return Message size.
     */
    private int sizeMessage(MessageEnvelope envelope) {
        int size = 0;
        if (envelope.getMessage() != null && envelope.getFile() == null) {
            size = envelope.getHeaders().length() + envelope.getMessage().length() + 2 + 2 + 5;
        } else if (envelope.getFile() != null) {
            size = (int) new File(envelope.getFile()).length() + 5;
        }

        return size;
    }
}

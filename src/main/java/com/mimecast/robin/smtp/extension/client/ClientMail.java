package com.mimecast.robin.smtp.extension.client;

import com.google.common.base.CharMatcher;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.transaction.EnvelopeTransactionList;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

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
        int messageID = connection.getSession().getSessionTransactionList().getEnvelopes().size();
        MessageEnvelope envelope = connection.getSession().getEnvelopes().get(messageID);

        // Construct delivery envelope.
        EnvelopeTransactionList transactionList = new EnvelopeTransactionList();

        // Check if envelope requires SMTPUTF8
        boolean smtpUtf8 = isUTF8(envelope.getMail().getBytes()) || envelope.getRcpts().stream().anyMatch(r -> isUTF8(r.getBytes()));

        // Sender.
        int size = sizeMessage(envelope);
        String write = "MAIL FROM:<" + envelope.getMail() + ">" + (size > 0 ? " SIZE=" + size : "") + (smtpUtf8 ? " SMTPUTF8" : "") + envelope.getParams("mail");
        connection.write(write);

        String read = connection.read("250");

        transactionList.addTransaction("MAIL", write, read, !read.startsWith("250"));

        // Add transaction list to envelope.
        connection.getSession().getSessionTransactionList().addEnvelope(transactionList);

        return read.startsWith("250");
    }

    /**
     * Checks if string is UTF-8 but not ASCII.
     *
     * @param bytes Byte array.
     * @return Boolean.
     */
    private boolean isUTF8(byte[] bytes) {
        if (CharMatcher.ascii().matchesAllOf(new String(bytes, StandardCharsets.UTF_8))) return false;

        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        try {
            decoder.decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException e) {
            return false;
        }

        return true;
    }

    /**
     * Get envelope size for MAIL parameter.
     *
     * @param envelope MessageEnvelope instance.
     * @return Message size.
     */
    private int sizeMessage(MessageEnvelope envelope) {
        int size = 0;
        if (envelope.getMessage() != null && envelope.getFile() == null && envelope.getFolder() == null) {
            size = envelope.buildHeaders().length() + envelope.getMessage().length() + 2 + 2 + 5;
        } else if (envelope.getFile() != null) {
            size = (int) new File(envelope.getFile()).length() + 5;
        }

        return size;
    }
}

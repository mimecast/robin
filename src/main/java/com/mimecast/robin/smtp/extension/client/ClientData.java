package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.io.ChunkedInputStream;
import com.mimecast.robin.smtp.io.MagicInputStream;
import com.mimecast.robin.smtp.transaction.EnvelopeTransactionList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * DATA extension processor.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class ClientData extends ClientProcessor {

    /**
     * MessageEnvelope instance.
     */
    private MessageEnvelope envelope;

    /**
     * EnvelopeTransactionList instance.
     */
    private EnvelopeTransactionList envelopeTransactions;

    /**
     * DATA processor.
     *
     * @param connection Connection instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection) throws IOException {
        super.process(connection);

        // Select message to send.
        int messageID = connection.getSessionTransactionList().getEnvelopes().size() - 1; // Adjust as it's initially added in ClientMail.

        // Select message envelope and transactions.
        envelope = connection.getSession().getEnvelopes().get(messageID);
        envelopeTransactions = connection.getSessionTransactionList().getEnvelopes().get(messageID);

        if (connection.getSession().isEhloBdat() && envelope.getChunkSize() >= 128) {
            return processBdat();
        } else {
            return processData();
        }
    }

    /**
     * DATA processor.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    private boolean processData() throws IOException {
        String write = "DATA";
        connection.write(write);

        String read;
        read = connection.read("354");
        if (!read.startsWith("354")) {
            envelopeTransactions.addTransaction(write, write, read, true);
            return false;
        }

        if (envelope.getFile() != null) {
            log.debug("Sending email from file: {}", envelope.getFile());
            connection.stream(new MagicInputStream(new FileInputStream(new File(envelope.getFile())), envelope));
        }

        else if (envelope.getStream() != null) {
            log.debug("Sending email from stream");
            connection.stream(new MagicInputStream(envelope.getStream(), envelope));
        }

        else if (envelope.getMessage() != null) {
            log.debug("Sending email from headers and body");
            connection.write(envelope.getHeaders());
            if (envelope.getMessage() != null) {
                connection.write(envelope.getMessage());
            }
        }

        log.debug("Sending [CRLF].[CRLF]");
        connection.write(".");

        read = connection.read("250");

        envelopeTransactions.addTransaction(write, write, read, !read.startsWith("250"));
        return read.startsWith("250");
    }

    /**
     * BDAT processor.
     * TODO Implement CHUNKING from stream.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    private boolean processBdat() throws IOException {
        String write;
        String read;

        if (envelope.getFile() != null) {
            int length;
            byte[] bdat;
            String sdat;
            ByteArrayOutputStream chunk;
            ChunkedInputStream chunks = new ChunkedInputStream(new FileInputStream(new File(envelope.getFile())), envelope);
            while(chunks.hasChunks()) {
                byte[] payload;

                chunk = chunks.getChunk();
                length = chunk.size();
                sdat = ("BDAT " + length + (!chunks.hasChunks() ? " LAST" : ""));
                bdat = (sdat + "\r\n").getBytes();

                // Merge bdat to first chunk.
                if (envelope.isChunkBdat()) {
                    payload = new byte[bdat.length + chunk.size()];
                    System.arraycopy(bdat, 0, payload, 0, bdat.length);
                    System.arraycopy(chunk.toByteArray(), 0, payload, bdat.length, chunk.size());
                } else {
                    connection.write(sdat);
                    payload = chunk.toByteArray();
                }

                connection.write(payload, envelope.isChunkWrite());

                read = connection.read("250");

                envelopeTransactions.addTransaction("BDAT", new String(bdat), read, !read.startsWith("250"));
                if(!read.startsWith("250")) return false;
            }
        }

        else if (envelope.getMessage() != null) {
            write = "BDAT " + (envelope.getHeaders().getBytes().length + 2 + envelope.getMessage().getBytes().length + 2) + " LAST";
            connection.write(write);
            connection.write(envelope.getHeaders());
            connection.write(envelope.getMessage());

            read = connection.read("250");

            envelopeTransactions.addTransaction("BDAT", write, read, !read.startsWith("250"));
            return read.startsWith("250");
        }

        return false;
    }
}

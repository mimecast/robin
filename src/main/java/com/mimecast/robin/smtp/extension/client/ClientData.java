package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.io.ChunkedInputStream;
import com.mimecast.robin.smtp.io.MagicInputStream;
import com.mimecast.robin.smtp.transaction.EnvelopeTransactionList;
import org.apache.commons.io.input.BoundedInputStream;

import java.io.*;

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

        // Configure data stream
        InputStream inputStream = null;

        if (envelope.getFile() != null) {
            log.debug("Sending email from file: {}", envelope.getFile());
            inputStream = new FileInputStream(new File(envelope.getFile()));

        } else if (envelope.getStream() != null) {
            log.debug("Sending email from stream.");
            inputStream = envelope.getStream();

        } else if (envelope.getMessage() != null) {
            log.debug("Sending email from headers and body.");
            inputStream = new ByteArrayInputStream((envelope.getHeaders() + "\r\n" + envelope.getMessage()).getBytes());
        }

        // Send data
        if (envelope.getTerminateAfterBytes() > 0) {
            log.debug("Terminating after {} bytes.", envelope.getTerminateAfterBytes());
            envelope.setTerminateBeforeDot(true);
            inputStream = new BoundedInputStream(inputStream, envelope.getTerminateAfterBytes());
        }

        // Send data
        if (inputStream != null) {
            connection.stream(
                    new MagicInputStream(inputStream, envelope),
                    envelope.getSlowBytes(),
                    envelope.getSlowWait()
            );
        }

        // Terminate before dot.
        if (envelope.isTerminateBeforeDot()) {
            log.debug("Terminating before <CRLF>.<CRLF>");
            connection.close();
            return false;
        }

        log.debug("Sending [CRLF].[CRLF]");
        connection.write(".");

        // Terminate after dot.
        if (envelope.isTerminateAfterDot()) {
            log.debug("Terminating after <CRLF>.<CRLF>");
            connection.close();
            return false;
        }

        read = connection.read("250");

        envelopeTransactions.addTransaction(write, write, read, !read.startsWith("250"));
        return read.startsWith("250");
    }

    /**
     * BDAT processor.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    private boolean processBdat() throws IOException {
        String read;

        if (envelope.getFile() != null || envelope.getStream() != null) {
            InputStream inputStream = null;

            // Choose file or stream
            if (envelope.getFile() != null) {
                inputStream = new FileInputStream(new File(envelope.getFile()));

            } else if (envelope.getStream() != null) {
                inputStream = envelope.getStream();
            }

            try (ChunkedInputStream chunks = new ChunkedInputStream(inputStream, envelope)) {
                ByteArrayOutputStream chunk;
                while (chunks.hasChunks()) {
                    chunk = chunks.getChunk();
                    read = writeChunk(chunk.toByteArray(), !chunks.hasChunks());

                    if (!read.startsWith("250")) return false;
                }
            }

        } else if (envelope.getMessage() != null) {
            // Write headers
            read = writeChunk((envelope.getHeaders() + "\r\n").getBytes(), false);
            if (!read.startsWith("250")) {
                return false;
            }

            // Write body
            read = writeChunk((envelope.getMessage() + "\r\n").getBytes(), true);
            return read.startsWith("250");
        }

        return false;
    }

    /**
     * Writes BDAT chunk to socket.
     *
     * @param chunk Chunk to write as byte array.
     * @param last  Is last chunk?
     * @return SMTP response string.
     * @throws IOException Unable to communicate.
     */
    private String writeChunk(byte[] chunk, boolean last) throws IOException {
        byte[] bdat = ("BDAT " + chunk.length + (last ? " LAST" : "") + "\r\n").getBytes();
        byte[] payload;

        // Merge bdat to first chunk.
        if (envelope.isChunkBdat()) {
            payload = new byte[bdat.length + chunk.length];
            System.arraycopy(bdat, 0, payload, 0, bdat.length);
            System.arraycopy(chunk, 0, payload, bdat.length, chunk.length);

        } else {
            connection.write(bdat);
            payload = chunk;
        }

        connection.write(payload, envelope.isChunkWrite(), envelope.getSlowBytes(), envelope.getSlowWait());

        String read = connection.read("250");
        envelopeTransactions.addTransaction("BDAT", new String(bdat), read, !read.startsWith("250"));

        return read;
    }
}

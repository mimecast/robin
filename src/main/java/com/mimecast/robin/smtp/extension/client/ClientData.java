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
     * TODO Implement slow from headers and body.
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
        }

        else if (envelope.getStream() != null) {
            log.debug("Sending email from stream.");
            inputStream = envelope.getStream();
        }

        else if (envelope.getMessage() != null) {
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
     * TODO Implement CHUNKING from stream.
     * TODO Implement slow from headers and body.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    private boolean processBdat() throws IOException {
        String write;
        String read;

        if (envelope.getFile() != null) {
            try(ChunkedInputStream chunks = new ChunkedInputStream(
                    new FileInputStream(new File(envelope.getFile())), envelope)) {

                int length;
                byte[] bdat;
                String sdat;
                ByteArrayOutputStream chunk;

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

                    connection.write(payload, envelope.isChunkWrite(), envelope.getSlowBytes(), envelope.getSlowWait());

                    read = connection.read("250");

                    envelopeTransactions.addTransaction("BDAT", new String(bdat), read, !read.startsWith("250"));
                    if(!read.startsWith("250")) return false;
                }
            }
        }

        else if (envelope.getMessage() != null) {
            // TODO Write headers and message separatly.
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

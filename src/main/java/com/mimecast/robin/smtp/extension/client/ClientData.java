package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.mime.EmailBuilder;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.io.ChunkedInputStream;
import com.mimecast.robin.smtp.io.MagicInputStream;
import com.mimecast.robin.smtp.transaction.EnvelopeTransactionList;
import com.mimecast.robin.util.StreamUtils;
import org.apache.commons.io.input.BoundedInputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * DATA extension processor.
 */
public class ClientData extends ClientProcessor {

    /**
     * MessageEnvelope instance.
     */
    protected int messageID;

    /**
     * MessageEnvelope instance.
     */
    protected MessageEnvelope envelope;

    /**
     * EnvelopeTransactionList instance.
     */
    protected EnvelopeTransactionList envelopeTransactions;

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
        messageID = connection.getSessionTransactionList().getEnvelopes().size() - 1; // Adjust as it's initially added in ClientMail.

        // Select message envelope and transactions.
        envelope = connection.getSession().getEnvelopes().get(messageID);
        envelopeTransactions = connection.getSessionTransactionList().getEnvelopes().get(messageID);

        // Evaluate is BDAT enabled.
        boolean bdat = connection.getSession().isEhloBdat() && envelope.getChunkSize() >= 128;

        // Configure data stream.
        InputStream inputStream = null;

        if (envelope.getMime() != null && !envelope.getMime().isEmpty()) {
            log.debug("Sending email from MIME.");

            Path path = Files.createTempFile("robin-", ".eml");
            try (Closeable ignored = () -> Files.delete(path)) {
                new EmailBuilder(connection.getSession(), envelope)
                        .buildMime()
                        .writeTo(new FileOutputStream(path.toFile()));

                inputStream = new FileInputStream(path.toFile());
            }

        } else if (envelope.getFile() != null) {
            log.debug("Sending email from file: {}", envelope.getFile());
            inputStream = new FileInputStream(envelope.getFile());

        } else if (envelope.getFolder() != null) {
            String file = envelope.getFolderFile();
            log.debug("Sending email from file: {}", file);
            inputStream = new FileInputStream(file);

        } else if (envelope.getStream() != null) {
            log.debug("Sending email from stream.");
            inputStream = envelope.getStream();

        } else if (envelope.getMessage() != null && !bdat) {
            log.debug("Sending email from headers and body.");
            inputStream = new ByteArrayInputStream((envelope.buildHeaders() + "\r\n" + envelope.getMessage()).getBytes());
        }

        boolean result;
        if (bdat) {
            result = processBdat(inputStream);

        } else {
            result = processData("DATA", inputStream);
        }

        StreamUtils.closeQuietly(inputStream);

        return result;
    }

    /**
     * DATA processor.
     *
     * @param verb        Verb.
     * @param inputStream InputStream instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @SuppressWarnings("SameParameterValue")
    protected boolean processData(String verb, InputStream inputStream) throws IOException {
        String write = verb != null ? verb : "DATA";
        connection.write(write);

        String read;
        read = connection.read("354");
        if (!read.startsWith("354")) {
            envelopeTransactions.addTransaction(write, write, read, true);
            return false;
        }

        // Send data
        if (inputStream != null) {
            if (envelope.getTerminateAfterBytes() > 0) {
                log.debug("Terminating after {} bytes.", envelope.getTerminateAfterBytes());
                envelope.setTerminateBeforeDot(true);
                inputStream = new BoundedInputStream(inputStream, envelope.getTerminateAfterBytes());
            }

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
     * @param inputStream InputStream instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    private boolean processBdat(InputStream inputStream) throws IOException {
        String read = "";

        if (inputStream != null) {
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
            read = writeChunk((envelope.buildHeaders() + "\r\n").getBytes(), false);
            if (!read.startsWith("250")) return false;

            // Write body
            read = writeChunk((envelope.getMessage() + "\r\n").getBytes(), true);
        }

        return read.startsWith("250");
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

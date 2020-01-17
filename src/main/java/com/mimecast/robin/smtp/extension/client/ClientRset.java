package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.smtp.connection.Connection;

import java.io.IOException;

/**
 * RSET extension processor.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class ClientRset extends ClientProcessor {

    String verb = "RSET";
    String code = "250";

    /**
     * RSET processor.
     *
     * @param connection Connection instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection) throws IOException {
        super.process(connection);

        try {
            connection.write(verb);

            String read = connection.read(code);
            connection.getSessionTransactionList().addTransaction(verb, verb, read);
        } catch (IOException e) {
            log.info("Error reading/writing for {}: {}", verb, e.getMessage());
        }

        return true;
    }
}

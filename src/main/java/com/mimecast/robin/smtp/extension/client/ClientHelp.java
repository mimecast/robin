package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.smtp.connection.Connection;

import java.io.IOException;

/**
 * HELP extension processor.
 */
public class ClientHelp extends ClientProcessor {

    /**
     * HELP processor.
     *
     * @param connection Connection instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection) throws IOException {
        super.process(connection);

        String write = "HELP";
        connection.write(write);

        String read = connection.read("214");
        connection.getSessionTransactionList().addTransaction(write, write, read, !read.startsWith("250"));

        return read.startsWith("214");
    }
}

package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.smtp.connection.Connection;

import java.io.IOException;

/**
 * QUIT extension processor.
 */
public class ClientQuit extends ClientRset {

    /**
     * QUIT processor.
     *
     * @param connection Connection instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection) throws IOException {
        verb = "QUIT";
        code = "221";
        return super.process(connection);
    }
}

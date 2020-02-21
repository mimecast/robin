package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.smtp.connection.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Client extension processor abstract.
 */
public abstract class ClientProcessor {
    static final Logger log = LogManager.getLogger(ClientProcessor.class);

    /**
     * Connection.
     */
    Connection connection;

    /**
     * Blank client processor.
     *
     * @param connection Connection instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    public boolean process(Connection connection) throws IOException {
        this.connection = connection;

        return true;
    }
}

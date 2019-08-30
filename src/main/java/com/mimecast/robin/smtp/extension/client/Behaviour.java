package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.smtp.connection.Connection;

import java.io.IOException;

/**
 * Client behaviour interface.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public interface Behaviour {

    /**
     * Process connection.
     *
     * @param connection Connection instance.
     * @throws IOException Unable to communicate.
     */
    void process(Connection connection) throws IOException;
}

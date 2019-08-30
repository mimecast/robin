package com.mimecast.robin.smtp.extension.server;

import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.verb.Verb;

import java.io.IOException;

/**
 * QUIT extension processor.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class ServerQuit extends ServerProcessor {

    /**
     * QUIT processor.
     *
     * @param connection Connection instance.
     * @param verb       Verb instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection, Verb verb) throws IOException {
        super.process(connection, verb);

        connection.write("221 2.0.0 Closing connection");
        connection.close();

        return false;
    }
}

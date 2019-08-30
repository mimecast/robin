package com.mimecast.robin.smtp.extension.server;

import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.verb.Verb;

import java.io.IOException;

/**
 * RSET extension processor.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class ServerRset extends ServerProcessor {

    /**
     * RSET processor.
     *
     * @param connection Connection instance.
     * @param verb Verb instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection, Verb verb) throws IOException {
        super.process(connection, verb);

        connection.write("250 2.1.5 All clear");
        connection.reset();

        return true;
    }
}

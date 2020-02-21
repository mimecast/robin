package com.mimecast.robin.smtp.extension.server;

import com.mimecast.robin.main.Extensions;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.verb.Verb;

import java.io.IOException;

/**
 * HELP extension processor.
 */
public class ServerHelp extends ServerProcessor {

    /**
     * HELP advert.
     *
     * @return Advert string.
     */
    @Override
    public String getAdvert() {
        return "HELP";
    }

    /**
     * HELP processor.
     *
     * @param connection Connection instance.
     * @param verb       Verb instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection, Verb verb) throws IOException {
        super.process(connection, verb);

        connection.write("214 " + Extensions.getHelp());

        return true;
    }
}

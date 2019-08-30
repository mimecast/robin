package com.mimecast.robin.smtp.extension.server;

import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.verb.Verb;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Server extension processor abstract.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public abstract class ServerProcessor {
    static final Logger log = LogManager.getLogger(ServerProcessor.class);

    /**
     * Connection instance.
     */
    Connection connection;

    /**
     * Verb instance.
     */
    Verb verb;

    /**
     * Advert getter.
     *
     * @return Advert string.
     */
    @SuppressWarnings("squid:S3400")
    String getAdvert() {
        return "";
    }

    /**
     * ClientProcessor.
     *
     * @param connection Connection instance.
     * @param verb       Verb instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    public boolean process(Connection connection, Verb verb) throws IOException {
        this.connection = connection;
        this.verb = verb;

        return true;
    }
}

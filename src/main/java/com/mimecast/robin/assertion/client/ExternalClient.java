package com.mimecast.robin.assertion.client;

import com.mimecast.robin.assertion.Assert;
import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.main.Factories;
import com.mimecast.robin.smtp.connection.Connection;

/**
 * Interface for external logs client.
 *
 * @see Assert
 * @see Factories
 */
public interface ExternalClient {

    /**
     * Sets Connection.
     *
     * @param connection Connection instance.
     * @return Self.
     */
    ExternalClient setConnection(Connection connection);

    /**
     * Sets Config.
     *
     * @param config Config instance.
     * @return Self.
     */
    ExternalClient setConfig(BasicConfig config);

    /**
     * Sets Transaction ID.
     *
     * @param transactionId Transaction ID.
     * @return Self.
     */
    ExternalClient setTransactionId(int transactionId);

    /**
     * Runs assertions.
     */
    void run() throws AssertException;
}

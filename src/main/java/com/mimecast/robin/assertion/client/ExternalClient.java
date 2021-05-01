package com.mimecast.robin.assertion.client;

import com.mimecast.robin.assertion.Assert;
import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.main.Factories;
import com.mimecast.robin.smtp.connection.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract for external logs client.
 *
 * @see Assert
 * @see Factories
 */
public abstract class ExternalClient {
    protected static final Logger log = LogManager.getLogger(ExternalClient.class);

    /**
     * Connection instance.
     */
    protected Connection connection;

    /**
     * Transaction ID.
     */
    protected int transactionId = -1;

    /**
     * Sets Connection.
     *
     * @param connection Connection instance.
     * @return Self.
     */
    public ExternalClient setConnection(Connection connection) {
        this.connection = connection;
        return this;
    }

    /**
     * Sets Config.
     *
     * @param config Config instance.
     * @return Self.
     */
    public abstract ExternalClient setConfig(BasicConfig config);

    /**
     * Sets Transaction ID.
     *
     * @param transactionId Transaction ID.
     * @return Self.
     */
    public ExternalClient setTransactionId(int transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    /**
     * Runs assertions.
     *
     * @throws AssertException Assertion exception.
     */
    public abstract void run() throws AssertException;
}

package com.mimecast.robin.assertion.client;

import com.mimecast.robin.assertion.Assert;
import com.mimecast.robin.assertion.AssertExternal;
import com.mimecast.robin.main.Factories;
import com.mimecast.robin.smtp.connection.Connection;
import org.json.JSONArray;

/**
 * Interface for external client.
 *
 * @see Assert
 * @see AssertExternal
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
     * Sets Transaction ID.
     *
     * @param transactionId Transaction ID.
     * @return Self.
     */
    ExternalClient setTransactionId(int transactionId);

    /**
     * Gets server logs.
     * <p>This is called by AssertExternal to get the logs if it has MTA assertions to do.
     *
     * @return JSONArray instance.
     */
    JSONArray getLogs();
}

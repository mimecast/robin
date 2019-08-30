package com.mimecast.robin.assertion.mta.client;

import com.mimecast.robin.assertion.Assert;
import com.mimecast.robin.assertion.mta.AssertMta;
import com.mimecast.robin.main.Factories;
import org.json.JSONArray;

/**
 * Interface for MTA logs client integration.
 * <p>The instanciation of this will be done via Factories.
 * <pre>
 *     logsClient = Factories.getLogsClient();
 *     logsClient.setServer(server);
 * <pre/>
 * <p>Server is provided so you may configure the logs client to know which server it should fetch from.
 *
 * @see Assert
 * @see AssertMta
 * @see Factories
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public interface LogsClient {

    /**
     * Sets TransactionList instance.
     * <p>This is mainly to provide server information.
     *
     * @param server String.
     */
    void setServer(String server);

    /**
     * Gets server logs.
     * <p>This is called by AssertMta to get the logs if it has MTA assertions to do.
     *
     * @param query Query string to query for.
     * @return Server logs.
     */
    JSONArray getLogs(String query);
}

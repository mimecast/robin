package com.mimecast.robin.assertion.client;

import com.mimecast.robin.assertion.Assert;
import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Factories;
import com.mimecast.robin.smtp.connection.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * Fail test for External verify failure.
     */
    protected Boolean assertVerifyFails;

    /**
     * Skip asserting and exit gracefully.
     */
    protected Boolean skip = false;

    /**
     * Sets Connection.
     *
     * @param connection Connection instance.
     * @return Self.
     */
    public ExternalClient setConnection(Connection connection) {
        this.connection = connection;
        this.assertVerifyFails = connection.getSession().getAssertions().getVerifyFails(Config.getClient().getAssertions().getVerifyFails());
        return this;
    }

    /**
     * Sets Config.
     *
     * @param config Config instance.
     * @return Self.
     */
    public ExternalClient setConfig(BasicConfig config) {
        magicReplace(config.getMap());
        return this;
    }

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

    /**
     * Runs assertions.
     */
    public Boolean skip() {
        return skip;
    }

    /**
     * Magic replace.
     *
     * @param map Map of String, Object.
     */
    @SuppressWarnings("unchecked")
    protected void magicReplace(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof String) {
                map.put(entry.getKey(), connection.getSession().magicReplace((String) entry.getValue()));

            } else if (entry.getValue() instanceof List) {
                List<Object> list = new ArrayList<>();

                List<Object> value = (List<Object>) entry.getValue();
                for (Object object : value) {
                    if (object instanceof String) {
                        object = connection.getSession().magicReplace((String) object);

                    } else if (object instanceof Map) {
                        magicReplace((Map<String, Object>) object);
                    }

                    list.add(object);
                }

                map.put(entry.getKey(), list);

            } else if (entry.getValue() instanceof Map) {
                magicReplace((Map<String, Object>) entry.getValue());
            }
        }
    }
}

package com.mimecast.robin.main;

import com.mimecast.robin.assertion.Assert;
import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.config.client.CaseConfig;
import com.mimecast.robin.smtp.EmailDelivery;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.session.Session;

import javax.naming.ConfigurationException;
import java.io.IOException;

/**
 * Case delivery client with assertion.
 *
 * @see Foundation
 * @see EmailDelivery
 */
public class Client extends Foundation {

    /**
     * Session instance.
     */
    protected Session session;

    /**
     * Connection instance.
     */
    protected Connection connection;

    /**
     * Have assertions been skipped?
     */
    protected Boolean skipped = false;

    /**
     * Constructs a new Client instance with given Session instance.
     *
     * @param session Session instance.
     */
    public Client(Session session) {
        this.session = session;
    }

    /**
     * Constructs a new Client instance.
     * <p>To be used in combination with the Junit launcher service.
     */
    public Client() {
        // Do nothing.
    }

    /**
     * Constructs a new Client instance with given client configuration path.
     *
     * @param configDirPath Directory path.
     * @throws ConfigurationException Unable to read/parse config file.
     */
    public Client(String configDirPath) throws ConfigurationException {
        init(configDirPath);
    }

    /**
     * Deliver email with given case configuration path.
     *
     * @param casePath File path.
     * @return Self.
     * @throws AssertException Assertion exception.
     * @throws IOException     Unable to communicate.
     */
    public Client send(String casePath) throws AssertException, IOException {
        return send(new CaseConfig(casePath));
    }

    /**
     * Deliver email with given CaseConfig instance.
     *
     * @param caseConfig CaseConfig instance.
     * @return Self.
     * @throws AssertException Assertion exception.
     */
    public Client send(CaseConfig caseConfig) throws AssertException {
        // Delivery Session.
        session = Factories.getSession();
        session.map(caseConfig);

        // Send.
        deliver();

        // Assert.
        assertion(connection);
        return this;
    }

    /**
     * Deliver email.
     */
    protected void deliver() {
        EmailDelivery emailDelivery = new EmailDelivery(session).send();
        connection = emailDelivery.getConnection();
    }

    /**
     * Assert delivery successfull if any.
     *
     * @param connection Connection instance.
     * @throws AssertException Assertion exception.
     */
    protected void assertion(Connection connection) throws AssertException {
        skipped = new Assert(connection).run().skipped();
    }

    /**
     * Have assertions been skipped?
     *
     * @return Boolean.
     */
    public boolean skipped() {
        return skipped;
    }

    /**
     * Gets Session instance.
     *
     * @return Session instance.
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets Connection instance.
     *
     * @return Connection instance.
     */
    public Connection getConnection() {
        return connection;
    }
}

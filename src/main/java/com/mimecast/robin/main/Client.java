package com.mimecast.robin.main;

import com.mimecast.robin.assertion.Assert;
import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.config.client.CaseConfig;
import com.mimecast.robin.smtp.EmailDelivery;
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
     * @throws AssertException Assertion exception.
     * @throws IOException     Unable to communicate.
     */
    public void send(String casePath) throws AssertException, IOException {
        send(new CaseConfig(casePath));
    }

    /**
     * Deliver email with given CaseConfig instance.
     *
     * @param caseConfig CaseConfig instance.
     * @throws AssertException Assertion exception.
     */
    public void send(CaseConfig caseConfig) throws AssertException {
        // Delivery Session.
        Session session = Factories.getSession();
        session.map(caseConfig);

        // Send.
        EmailDelivery emailDelivery = new EmailDelivery(session).send();

        // Assert.
        assertion(emailDelivery);
    }

    /**
     * Assert delivery successfull if any.
     *
     * @param emailDelivery EmailDelivery instance.
     * @throws AssertException Assertion exception.
     */
    protected void assertion(EmailDelivery emailDelivery) throws AssertException {
        new Assert(emailDelivery.getConnection()).run();
    }
}

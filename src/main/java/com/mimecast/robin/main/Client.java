package com.mimecast.robin.main;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.config.client.CaseConfig;
import com.mimecast.robin.smtp.EmailDelivery;
import com.mimecast.robin.smtp.session.Session;

import javax.naming.ConfigurationException;
import java.io.IOException;

/**
 * Email delivery client.
 * <p>This is the client used to send cases.
 * <p>It takes a configuration directory path and a case JSON path.
 * <p>The configuration path is used to load the global configuration files.
 * <p>Loads both client and server configuration files.
 *
 * @see Foundation
 * @see EmailDelivery
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class Client extends Foundation {

    /**
     * Constructs a new Client instance with given client configuration path.
     *
     * @param configDirPath Directory path.
     */
    public Client(String configDirPath) throws ConfigurationException {
        init(configDirPath);
    }

    /**
     * Deliver email with given case configuration path.
     *
     * @param casePath File path.
     * @throws AssertException Assertion exception.
     */
    public void send(String casePath) throws AssertException, IOException {
        CaseConfig caseConfig = new CaseConfig(casePath);

        send(caseConfig);
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
        new EmailDelivery(session).send();
    }
}

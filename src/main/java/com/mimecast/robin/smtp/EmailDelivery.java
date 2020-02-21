package com.mimecast.robin.smtp;

import com.mimecast.robin.main.Factories;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.connection.SmtpException;
import com.mimecast.robin.smtp.extension.client.Behaviour;
import com.mimecast.robin.smtp.session.Session;
import com.mimecast.robin.smtp.transaction.EnvelopeTransactionList;
import com.mimecast.robin.smtp.transaction.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Email delivery core.
 *
 * <p>This is used to connect and deliver emails based on given Session.
 */
public class EmailDelivery {
    private static final Logger log = LogManager.getLogger(EmailDelivery.class);

    /**
     * Connection instance.
     */
    Connection connection;

    /**
     * Constructs a new EmailDelivery instance with given Session.
     *
     * @param session Session instance.
     */
    public EmailDelivery(Session session) {
        this.connection = new Connection(session);
    }

    /**
     * Gets connection.
     *
     * @return Connection instance.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Send.
     * <p>Main executable.
     *
     * @return Self.
     */
    public EmailDelivery send() {
        try {
            connection.connect();
            log.debug("Remote ready and willing.");

            Behaviour behaviour = Factories.getBehaviour();
            if (behaviour != null) {
                behaviour.process(connection);
            } else log.error("Error getting behaviour. This is bad.");

            log.debug("Terminating communication.");
            terminate();

        } catch (SmtpException e) {
            log.info("Error in SMTP exchange: {}", e.getMessage());
            terminate();

        } catch (IOException e) {
            log.info("Error reading/writing: {}", e.getMessage());
            connection.getSessionTransactionList().addTransaction("SMTP", "101 " + e.getMessage(), true);

        } finally {
            connection.close();
        }

        return this;
    }

    /**
     * Close connection.
     */
    private void terminate() {
        // Session.
        logErrors(connection.getSessionTransactionList().getErrors());

        // Envelope.
        for (EnvelopeTransactionList envelope : connection.getSessionTransactionList().getEnvelopes()) {
            logErrors(envelope.getErrors());
        }

        connection.close();
    }

    /**
     * Log errors.
     *
     * @param errors List of Transaction instances.
     */
    private void logErrors(List<Transaction> errors) {
        if (!errors.isEmpty()) {
            log.info("Errors in delivery: {}", errors.size());
            for (Transaction t : errors) {
                log.info("Error: {}", t.getResponse());
            }
        }
    }
}

package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Extensions;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.extension.Extension;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

/**
 * Default client behaviour.
 */
public class DefaultBehaviour implements Behaviour {
    protected static final Logger log = LogManager.getLogger(DefaultBehaviour.class);

    /**
     * Connection.
     */
    Connection connection;

    /**
     * Executes delivery.
     *
     * @param connection Connection instance.
     * @throws IOException Unable to communicate.
     */
    @Override
    public void process(Connection connection) throws IOException {
        this.connection = connection;

        if (!ehlo()) return;
        if (!startTls()) return;
        if (!auth()) return;
        if (!connection.getSession().getEnvelopes().isEmpty()) {
            data();
        }
        quit();
    }

    /**
     * Executes EHLO.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    boolean ehlo() throws IOException {
        // HELO/EHLO
        if (connection.getSession().getEhlo() != null) {
            return process("ehlo", connection);
        }

        return false;
    }

    /**
     * Executes STARTTLS.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    boolean startTls() throws IOException {
        return !process("starttls", connection) || !connection.getSession().isStartTls() || ehlo();
    }

    /**
     * Executes AUTH.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean auth() throws IOException {
        // Authenticate if configured.
        if (connection.getSession().isAuth()) {
            if (!process("auth", connection)) return false;

            // Start TLS if specifically configured to do after AUTH.
            if (connection.getSession().isAuthBeforeTls()) {
                return startTls();
            }
        }

        return true;
    }

    /**
     * Executes DATA.
     *
     * @throws IOException Unable to communicate.
     */
    void data() throws IOException {
        for (int i = 0; i < connection.getSession().getEnvelopes().size(); i++) {
            MessageEnvelope envelope = connection.getSession().getEnvelopes().get(i);
            for (int j = 0; j < envelope.getRepeat() + 1; j++) {
                // Reset transaction.
                if (j > 0) {
                    connection.getSessionTransactionList().getEnvelopes().remove(connection.getSessionTransactionList().getEnvelopes().size() - 1);
                }

                if (Config.getProperties().getBooleanProperty("rsetBetweenEnvelopes", false)) {
                    if ((i > 0 || j > 0) && !process("rset", connection)) return;
                }
                send();
            }
        }
    }

    /**
     * Executes envelope delivery.
     *
     * @throws IOException Unable to communicate.
     */
    private void send() throws IOException {
        if (!process("mail", connection)) return;
        if (!process("rcpt", connection)) return;
        process("data", connection);
    }

    /**
     * Executes QUIT.
     *
     * @throws IOException Unable to communicate.
     */
    void quit() throws IOException {
        process("quit", connection);
    }

    /**
     * Processes extension.
     *
     * @param extension  String.
     * @param connection Connection instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    boolean process(String extension, Connection connection) throws IOException {
        Optional<Extension> opt = Extensions.getExtension(extension);
        return opt.isPresent() && opt.get().getClient().process(connection);
    }
}

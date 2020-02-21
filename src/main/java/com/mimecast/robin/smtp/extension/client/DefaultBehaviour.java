package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.main.Extensions;
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
     * Executes DATA.
     *
     * @throws IOException Unable to communicate.
     */
    void data() throws IOException {
        for (int i = 0; i < connection.getSession().getEnvelopes().size(); i++) {
            if (i > 0 && !process("rset", connection)) return;
            send();
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
        Optional<Extension> opt = Extensions.getExtension("quit");
        if (opt.isPresent()) {
            opt.get().getClient().process(connection);
        }
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

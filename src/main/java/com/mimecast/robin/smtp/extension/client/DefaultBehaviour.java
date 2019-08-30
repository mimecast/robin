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
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class DefaultBehaviour implements Behaviour {
    protected static final Logger log = LogManager.getLogger(DefaultBehaviour.class);

    /**
     * Connection.
     */
    Connection connection;

    /**
     * Executes delivery.
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
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean auth() throws IOException {
        // Authenticate if configured.
        if (connection.getSession().isAuth()) {
            if (!process("auth", connection)) return false;

            // Start TLS if specifically configured to do after AUTH.
            if(connection.getSession().isAuthTls()) {
                return startTls();
            }
        }

        return true;
    }

    /**
     * Executes EHLO.
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
     */
    boolean startTls() throws IOException {
        return !process("starttls", connection) || !connection.getSession().isStartTls() || ehlo();
    }

    /**
     * Executes DATA.
     */
    void data() throws IOException {
        for (int i = 0; i < connection.getSession().getEnvelopes().size(); i++) {
            if (i > 0 && !process("rset", connection)) return;
            send();
        }
    }

    /**
     * Executes envelope delivery.
     */
    private void send() throws IOException {
        if (!process("mail", connection)) return;
        if (!process("rcpt", connection)) return;
        process("data", connection);
    }

    /**
     * Executes QUIT.
     */
    void quit() throws IOException {
        Optional<Extension> opt = Extensions.getExtension("quit");
        if (opt.isPresent()) {
            opt.get().getClient().process(connection);
        }
    }

    /**
     * Processes extension.
     */
    boolean process(String extension, Connection connection) throws IOException {
        Optional<Extension> opt = Extensions.getExtension(extension);
        return opt.isPresent() && opt.get().getClient().process(connection);
    }
}

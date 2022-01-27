package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.main.Extensions;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.extension.Extension;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Customisable client behaviour.
 */
public class CustomBehaviour implements Behaviour {
    protected static final Logger log = LogManager.getLogger(CustomBehaviour.class);

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

        // Select message and envelope to send.
        int messageID = !connection.getSessionTransactionList().getEnvelopes().isEmpty() ?
                connection.getSessionTransactionList().getEnvelopes().size() - 1 :
                0; // Adjust as it's initially added in ClientMail or 0 if none yet.
        MessageEnvelope envelope = connection.getSession().getEnvelopes().get(messageID);

        // Get recipients copy.
        List<String> recipients = new ArrayList<>(envelope.getRcpts());
        int recipient = 0;

        for (String extension : connection.getSession().getBehaviour()) {
            switch (extension.toUpperCase()) {
                case "EHLO":
                    if (!ehlo()) return;
                    break;
                case "STARTTLS":
                    if (!startTls()) return;
                    break;
                case "AUTH":
                    if (!auth()) return;
                    break;
                case "MAIL":
                    if (!mail()) return;
                    break;
                case "RCPT":
                    // Clear recpipients and add just one then increment.
                    envelope.getRcpts().clear();
                    envelope.getRcpts().add(recipients.get(recipient));
                    recipient++;

                    if (!rcpt()) return;
                    break;
                case "DATA":
                    if (!data()) return;
                    break;
            }
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
     * Executes AUTH.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean auth() throws IOException {
        // Authenticate if configured.
        if (connection.getSession().isAuth()) {
            return process("auth", connection);
        }

        return true;
    }

    /**
     * Executes STARTTLS.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    boolean startTls() throws IOException {
        return process("starttls", connection);
    }

    /**
     * Executes MAIL.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    boolean mail() throws IOException {
        return process("mail", connection);
    }

    /**
     * Executes RCPT.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    boolean rcpt() throws IOException {
        return process("rcpt", connection);
    }

    /**
     * Executes DATA.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    boolean data() throws IOException {
        return process("data", connection);
    }

    /**
     * Executes RSET.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    boolean rset() throws IOException {
        return process("rset", connection);
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

package com.mimecast.robin.smtp;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Extensions;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.extension.Extension;
import com.mimecast.robin.smtp.verb.Verb;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

/**
 * Email receipt runnable.
 *
 * <p>This is used to create threads for incoming connections.
 * <p>A new instance will be constructed for every socket connection the server receives.
 */
@SuppressWarnings("WeakerAccess")
public class EmailReceipt implements Runnable {
    private static final Logger log = LogManager.getLogger(EmailReceipt.class);

    /**
     * Connection instance.
     */
    protected Connection connection;

    /**
     * Transactions limitation.
     * <p>Limits how many commands will be processed.
     */
    private final int transactionsLimit = Config.getServer().getTransactionsLimit();

    /**
     * Error limitation.
     * <p>Limits how many eronious commands will be permitted.
     */
    private int errorLimit = Config.getServer().getErrorLimit();

    /**
     * Constructs a new EmailReceipt instance with given Connection instance.
     * <p>For testing purposes only.
     *
     * @param connection Connection instance.
     */
    EmailReceipt(Connection connection) {
        this.connection = connection;
    }

    /**
     * Constructs a new EmailReceipt instance with given socket.
     *
     * @param socket Inbound socket.
     */
    public EmailReceipt(Socket socket) {
        try {
            connection = new Connection(socket);
        } catch (IOException e) {
            log.info("Error initializing streams: {}", e.getMessage());
        }
    }

    /**
     * Server receipt runner.
     * <p>The loop begins after a connection is received and the welcome message sent.
     * <p>It will stop is processing any command returns false.
     * <p>False can be returned is there was a problem processing said command or from QUIT.
     * <p>The loop will also break if the syntax error limit is reached.
     * <p>Once the loop breaks the connection is closed.
     */
    public void run() {
        try {
            connection.write("220 Robin ready at " + connection.getSession().getRdns() + " with ESMTP; " + connection.getSession().getDate());

            Verb verb;
            for (int i = 0; i < transactionsLimit; i++) {
                String read = connection.read().trim();
                verb = new Verb(read);

                // Don't process if error.
                if (!isError(verb)) process(verb);

                // Break the loop.
                // Break if error limit reached.
                if (verb.getCommand().equalsIgnoreCase("quit") || errorLimit <= 0) {
                    if (errorLimit <= 0) {
                        log.warn("Error limit reached.");
                    }
                    break;
                }
            }
        } catch (IOException e) {
            log.info("Error reading/writing: {}", e.getMessage());
        }

        connection.close();
    }

    /**
     * Server extension processor.
     *
     * @param verb Verb instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    private boolean isError(Verb verb) throws IOException {
        if (verb.isError()) {
            connection.write("500 Syntax error");
            errorLimit--;
            return true;
        }

        return false;
    }

    /**
     * Server extension processor.
     *
     * @param verb Verb instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    private boolean process(Verb verb) throws IOException {
        if (Extensions.isExtension(verb)) {
            Optional<Extension> opt = Extensions.getExtension(verb);
            if (opt.isPresent()) {
                return opt.get().getServer().process(connection, verb);
            }
        } else {
            errorLimit--;
            if (errorLimit == 0) {
                log.warn("Error limit reached.");
                return false;
            }

            connection.write("500 5.3.3 Unrecognized command");
        }

        return true;
    }
}

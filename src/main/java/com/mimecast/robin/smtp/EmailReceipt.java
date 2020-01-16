package com.mimecast.robin.smtp;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Extensions;
import com.mimecast.robin.smtp.extension.Extension;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.verb.Verb;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

/**
 * Email receipt runnable.
 * <p>This is used to create threads for incoming connections.
 * <p>A new instance will be constructed for every socket connection the server receives.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
@SuppressWarnings("WeakerAccess")
public class EmailReceipt implements Runnable {
    private static final Logger log = LogManager.getLogger(EmailReceipt.class);

    /**
     * Connection instance.
     */
    private Connection connection;

    /**
     * Error limitation.
     * <p>Limits how many eronious commands will be permitted.
     */
    private int errorLimit = Config.getServer().getErrorLimit();

    /**
     * Constructs a new EmailReceipt instance with given socket.
     *
     * @param socket Inbound socket.
     */
    public EmailReceipt(Socket socket) {
        try {
            connection = new Connection(socket);
        } catch (IOException e) {
            log.error("Error initializing streams: {}", e.getMessage());
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
            connection.write("220 " + connection.getSession().getRdns() + " ESMTP; " + connection.getSession().getDate());

            boolean loop = true;
            Verb verb;
            while (loop) {
                String read = connection.read().trim();
                verb = new Verb(read);

                // Don't process if error received.
                if (!isError(verb)) loop = process(verb);

                // Break if error limit reached.
                if (errorLimit <= 0) {
                    log.warn("Error limit reached.");
                    break;
                }
            }
        } catch (IOException e) {
            log.error("Error reading/writing: {}", e.getMessage());
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
        }
        else {
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

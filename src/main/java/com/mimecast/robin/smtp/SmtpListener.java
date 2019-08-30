package com.mimecast.robin.smtp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.SocketException;

/**
 * SMTP socket listener.
 * <p>This runs a ServerSocket bound to configured interface and port.
 * <p>An email receipt instance will be constructed for each accepted connection.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 * @see EmailReceipt
 */
public class SmtpListener {
    private static final Logger log = LogManager.getLogger(SmtpListener.class);

    /**
     * ServerSocket instance.
     */
    private ServerSocket listener;

    /**
     * Server shutdown boolean.
     */
    private boolean serverShutdown = false;

    /**
     * Constructs a new SmtpListener instance.
     *
     * @param port    Port number.
     * @param backlog Backlog size.
     * @param bind    Interface to bind to.
     */
    public SmtpListener(int port, int backlog, String bind) {
        try (ServerSocket socket = new ServerSocket(port, backlog, InetAddress.getByName(bind))) {
            listener = socket;
            log.info("Started listener.");

            log.info("Expecting connection.");
            acceptConnection();
        } catch (IOException e) {
            log.error("Unable to start listener: {}", e.getMessage());
        } finally {
            try {
                if (listener != null) {
                    listener.close();
                    log.info("Closed listener.");
                }
            } catch (Exception e) {
                log.error("Listener already closed.");
            }
        }
    }

    /**
     * Accept incomming connection.
     */
    private void acceptConnection() {
        try {
            do {
                Socket sock = listener.accept();
                log.info("Accepted connection from {}:{}", sock.getInetAddress().getHostAddress(), sock.getPort());
                new Thread(new EmailReceipt(sock)).start();
            } while (!serverShutdown);

        } catch (SocketException e) {
            log.error("Listener closed.");

        } catch (IOException e) {
            log.error("Unable to accept connection.");
        }
    }

    /**
     * Shutdown.
     *
     * @throws IOException Unable to communicate.
     */
    public void serverShutdown() throws IOException {
        serverShutdown = true;
        if (listener != null) {
            listener.close();
        }
    }

    /**
     * Gets listener.
     *
     * @return ServerSocket instance.
     */
    public ServerSocket getListener() {
        return listener;
    }
}

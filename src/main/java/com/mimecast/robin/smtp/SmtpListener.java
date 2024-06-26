package com.mimecast.robin.smtp;

import com.mimecast.robin.main.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * SMTP socket listener.
 *
 * <p>This runs a ServerSocket bound to configured interface and port.
 * <p>An email receipt instance will be constructed for each accepted connection.
 *
 * @see EmailReceipt
 */
public class SmtpListener {
    private static final Logger log = LogManager.getLogger(SmtpListener.class);

    /**
     * ServerSocket instance.
     */
    private ServerSocket listener;

    /**
     * ThreadPoolExecutor instance.
     */
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

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
        configure();

        try (ServerSocket socket = new ServerSocket(port, backlog, InetAddress.getByName(bind))) {
            listener = socket;
            log.info("Started listener.");

            log.info("Expecting connection.");
            acceptConnection();

        } catch (IOException e) {
            log.fatal("Error listening: {}", e.getMessage());

        } finally {
            try {
                if (listener != null) {
                    listener.close();
                    log.info("Closed listener.");
                }
                executor.shutdown();
            } catch (Exception e) {
                log.info("Listener already closed.");
            }
        }
    }

    /**
     * Configure thread pool.
     */
    protected void configure() {
        executor.setKeepAliveTime(Config.getServer().getThreadKeepAliveTime(), TimeUnit.SECONDS);
        executor.setCorePoolSize(Config.getServer().getMinimumPoolSize());
        executor.setMaximumPoolSize(Config.getServer().getMaximumPoolSize());
    }

    /**
     * Accept incomming connection.
     */
    private void acceptConnection() {
        try {
            do {
                Socket sock = listener.accept();
                log.info("Accepted connection from {}:{}.", sock.getInetAddress().getHostAddress(), sock.getPort());

                executor.submit(() -> {
                    new EmailReceipt(sock).run();
                    return null;
                });
            } while (!serverShutdown);

        } catch (SocketException e) {
            log.info("Error in socket exchange: {}", e.getMessage());

        } catch (IOException e) {
            log.info("Error reading/writing: {}", e.getMessage());
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
        executor.shutdown();
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

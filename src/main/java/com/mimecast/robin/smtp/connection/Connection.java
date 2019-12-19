package com.mimecast.robin.smtp.connection;

import com.mimecast.robin.config.server.ScenarioConfig;
import com.mimecast.robin.config.server.ServerConfig;
import com.mimecast.robin.config.server.UserConfig;
import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Factories;
import com.mimecast.robin.smtp.EmailDelivery;
import com.mimecast.robin.smtp.EmailReceipt;
import com.mimecast.robin.smtp.io.LineInputStream;
import com.mimecast.robin.smtp.session.Session;
import com.mimecast.robin.smtp.transaction.SessionTransactionList;
import com.mimecast.robin.util.Sleep;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;

/**
 * Connection container.
 * <p>This is the object that passes through all the implementations of the extensions used in a receipt or delivery.
 *
 * @see EmailDelivery
 * @see EmailReceipt
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class Connection extends SmtpFoundation {
    private static final Logger log = LogManager.getLogger(Connection.class);

    /**
     * Session instance.
     */
    private Session session = null;

    /**
     * SessionTransactionList instance.
     */
    private SessionTransactionList sessionTransactionList = new SessionTransactionList();

    /**
     * Connection server.
     */
    private String server = null;

    /**
     * [Client] Constructs a new Connection instance with given Session.
     * <p>This is primarly here for unit testing.
     *
     * @param session Session instance.
     */
    public Connection(Session session, SessionTransactionList sessionTransactionList) {
        this(session);
        this.sessionTransactionList = sessionTransactionList;
    }

    /**
     * [Client] Constructs a new Connection instance with given Session.
     *
     * @param session Session instance.
     */
    public Connection(Session session) {
        this.session = session;
    }

    /**
     * [Server] Constructs a new Connection instance with given Socket.
     *
     * @param socket Socket instance.
     * @throws IOException Unable to communicate.
     */
    public Connection(Socket socket) throws IOException {
        // Socket.
        this.socket = socket;
        setTimeout(DEFAULTTIMEOUT);

        // Streams.
        buildStreams();

        // Session.
        if (session == null) {
            session = Factories.getSession();
        }

        // Connection info.
        session.setAddr(socket.getLocalAddress().getHostName());
        session.setRdns(socket.getLocalAddress().getHostAddress());

        session.setFriendAddr(socket.getInetAddress().getHostAddress());
        session.setFriendRdns(socket.getInetAddress().getHostName());
    }

    /**
     * Connect to socket.
     *
     * @throws IOException Unable to communicate.
     */
    public void connect() throws IOException {
        if (StringUtils.isNotBlank(session.getBind())) {
            log.info("Binding to: {}", session.getBind());
            socket.bind(new InetSocketAddress(session.getBind(), 0));
        }

        int retry = session.getRetry() > 0 ? session.getRetry() : 1;

        for (int i = 0; i < retry; i++) {
            server = session.getMx().get(i % session.getMx().size());
            try {
                log.info("Connecting to: {}:{}", server, session.getPort());
                socket = new Socket();
                socket.connect(new InetSocketAddress(server, session.getPort()));

                buildStreams();

                log.info("Connected to: {}:{}", server, session.getPort());

                String read = read("220");
                sessionTransactionList.addTransaction("SMTP", read, !read.startsWith("220"));

                if (!read.startsWith("220")) {
                    if (i == retry - 1) {
                        throw new SmtpException("SMTP");
                    } else {
                        close();
                    }
                }
            } catch (IOException e) {
                if (i == retry - 1) {
                    throw e;
                }
                log.debug("Failed: {}", e.getMessage());
                Sleep.nap(session.getDelay() * 1000);
            }
        }
    }

    /**
     * Build input/output streams.
     *
     * @throws IOException Unable to communicate.
     */
    public void buildStreams() throws IOException {
        inc = new LineInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    /**
     * Gets Session instance.
     *
     * @return Session instance.
     */
    public Session getSession() {
        return session;
    }

    /**
     * [Client] Gets SessionTransactionList instance.
     *
     * @return SessionTransactionList instance.
     */
    public SessionTransactionList getSessionTransactionList() {
        return sessionTransactionList;
    }

    /**
     * Gets connection server.
     *
     * @return Server string.
     */
    public String getServer() {
        return server;
    }

    /**
     * [Server] Gets server username.
     *
     * @param username Username string.
     * @return Optional of UserConfig.
     */
    public Optional<UserConfig> getUser(String username) {
        return Config.getServer().getUser(username);
    }

    /**
     * [Server] Gets scenarios for given HELO/EHLO.
     *
     * @return Optional of ScenarioConfig.
     */
    public Optional<ScenarioConfig> getScenario() {
        return Optional.ofNullable(Config.getServer())
                .map(ServerConfig::getScenarios)
                .map(s -> s.get(session.getEhlo()));
    }

    /**
     * [Server] Reset connection.
     */
    @SuppressWarnings("EmptyMethod")
    public void reset() {
        // TODO Implement reset.
    }
}

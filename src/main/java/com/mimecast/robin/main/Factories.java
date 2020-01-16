package com.mimecast.robin.main;

import com.mimecast.robin.annotation.Plugin;
import com.mimecast.robin.assertion.mta.client.LogsClient;
import com.mimecast.robin.smtp.auth.DigestCache;
import com.mimecast.robin.smtp.auth.StaticDigestCache;
import com.mimecast.robin.smtp.extension.client.Behaviour;
import com.mimecast.robin.smtp.extension.client.DefaultBehaviour;
import com.mimecast.robin.smtp.security.DefaultTLSSocket;
import com.mimecast.robin.smtp.security.PermissiveTrustManager;
import com.mimecast.robin.smtp.security.TLSSocket;
import com.mimecast.robin.smtp.session.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.X509TrustManager;
import java.util.concurrent.Callable;

/**
 * Factories for pluggable components.
 * <p>This is a factories container for extensible components.
 * <p>You may write a plugin to inject yours.
 *
 * @see Plugin
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class Factories {
    private static final Logger log = LogManager.getLogger(Factories.class);

    /**
     * SMTP client behaviour.
     * <p>The logic of the client.
     */
    private static Callable<Behaviour> behaviour;

    /**
     * SMTP Session.
     * <p>Used by both client and server.
     */
    private static Callable<Session> session;

    /**
     * TLS socket implementation.
     * <p>Implements TLS handshake.
     */
    private static Callable<TLSSocket> tlsSocket;

    /**
     * Trust manager implementation.
     * <p>Implements javax.net.ssl.X509TrustManager.
     */
    private static Callable<X509TrustManager> trustManager;

    /**
     * Digest MD5 database.
     * <p>Only used for subsequent authentication.
     */
    private static Callable<DigestCache> database;

    /**
     * MTA logs client.
     * <p>Used to fetch MTA logs for assertion.
     */
    private static Callable<LogsClient> logsClient;

    /**
     * Protected constructor.
     */
    private Factories() {
        throw new IllegalStateException("Static class");
    }

    /**
     * Sets Behaviour.
     *
     * @param callable Behaviour callable.
     */
    public static void setBehaviour(Callable<Behaviour> callable) {
        behaviour = callable;
    }

    /**
     * Gets Behaviour.
     *
     * @return Behaviour instance.
     */
    public static Behaviour getBehaviour() {
        if (behaviour != null) {
            try {
                return behaviour.call();
            } catch (Exception e) {
                log.error("Error calling behaviour: {}", e.getMessage());
            }
        }

        return new DefaultBehaviour();
    }

    /**
     * Sets Session.
     *
     * @param callable Session callable.
     */
    public static void setSession(Callable<Session> callable) {
        session = callable;
    }

    /**
     * Gets Session.
     *
     * @return Session instance.
     */
    public static Session getSession() {
        if (session != null) {
            try {
                return session.call();
            } catch (Exception e) {
                log.error("Error calling session: {}", e.getMessage());
            }
        }

        return new Session();
    }

    /**
     * Sets TLSSocket.
     *
     * @param callable TLSSocket callable.
     */
    public static void setTLSSocket(Callable<TLSSocket> callable) {
        tlsSocket = callable;
    }

    /**
     * Gets TLSSocket.
     *
     * @return TLSSocket instance.
     */
    public static TLSSocket getTLSSocket() {
        if (tlsSocket != null) {
            try {
                return tlsSocket.call();
            } catch (Exception e) {
                log.error("Error calling TLS socket: {}", e.getMessage());
            }
        }

        return new DefaultTLSSocket();
    }

    /**
     * Sets TrustManager.
     *
     * @param callable TrustManager callable.
     */
    public static void setTrustManager(Callable<X509TrustManager> callable) {
        trustManager = callable;
    }

    /**
     * Gets TrustManager.
     *
     * @return TrustManager instance.
     */
    public static X509TrustManager getTrustManager() {
        if (trustManager != null) {
            try {
                return trustManager.call();
            } catch (Exception e) {
                log.error("Error calling trust manager: {}", e.getMessage());
            }
        }

        return new PermissiveTrustManager();
    }

    /**
     * Sets DigestDatabase.
     *
     * @param callable DigestDatabase callable.
     */
    public static void setDatabase(Callable<DigestCache> callable) {
        database = callable;
    }

    /**
     * Gets DigestDatabase.
     *
     * @return DigestDatabase instance.
     */
    public static DigestCache getDatabase() {
        if (database != null) {
            try {
                return database.call();
            } catch (Exception e) {
                log.error("Error calling database: {}", e.getMessage());
            }
        }

        return new StaticDigestCache();
    }

    /**
     * Sets LogsClient.
     *
     * @param callable LogsClient callable.
     */
    public static void setLogsClient(Callable<LogsClient> callable) {
        logsClient = callable;
    }

    /**
     * Gets LogsClient.
     *
     * @return LogsClient instance.
     */
    public static LogsClient getLogsClient() {
        if (logsClient != null) {
            try {
                return logsClient.call();
            } catch (Exception e) {
                log.error("Error calling logs client: {}", e.getMessage());
            }
        }

        return null;
    }
}

package com.mimecast.robin.main;

import com.mimecast.robin.annotation.Plugin;
import com.mimecast.robin.assertion.client.ExternalClient;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.smtp.auth.DigestCache;
import com.mimecast.robin.smtp.auth.StaticDigestCache;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.extension.client.Behaviour;
import com.mimecast.robin.smtp.extension.client.DefaultBehaviour;
import com.mimecast.robin.smtp.security.DefaultTLSSocket;
import com.mimecast.robin.smtp.security.PermissiveTrustManager;
import com.mimecast.robin.smtp.security.TLSSocket;
import com.mimecast.robin.smtp.session.Session;
import com.mimecast.robin.storage.LocalStorageClient;
import com.mimecast.robin.storage.StorageClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.X509TrustManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Factories for pluggable components.
 *
 * <p>This is a factories container for extensible components.
 * <p>You may write a plugin to inject yours.
 *
 * @see Plugin
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
     * MTA storage client.
     * <p>Used to store MTA emails received.
     */
    private static Callable<StorageClient> storageClient;

    /**
     * External clients.
     * <p>Used to fetch external service logs for assertion.
     */
    private static final Map<String, Callable<ExternalClient>> externalClients = new HashMap<>();

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
     * Sets StorageClient.
     *
     * @param callable StorageClient callable.
     */
    public static void setStorageClient(Callable<StorageClient> callable) {
        storageClient = callable;
    }

    /**
     * Gets StorageClient.
     *
     * @param extension  File extension.
     * @param connection Connection instance.
     * @return StorageClient instance.
     */
    public static StorageClient getStorageClient(Connection connection, String extension) {
        if (storageClient != null) {
            try {
                return storageClient.call().setConnection(connection);
            } catch (Exception e) {
                log.error("Error calling storage client: {}", e.getMessage());
            }
        }

        return new LocalStorageClient(extension).setConnection(connection);
    }

    /**
     * Puts ExternalClient.
     *
     * @param key      Config map key.
     * @param callable ExternalClient callable.
     */
    public static void putExternalClient(String key, Callable<ExternalClient> callable) {
        externalClients.put(key, callable);
    }

    /**
     * Gets ExternalClient by key.
     *
     * @param key        Config map key.
     * @param connection Connection instance.
     * @param config     BasicConfig instance.
     * @return ExternalClient instance.
     */
    public static ExternalClient getExternalClient(String key, Connection connection, BasicConfig config) {
        if (externalClients.get(key) != null) {
            try {
                return externalClients.get(key).call().setConnection(connection)
                        .setConfig(config);
            } catch (Exception e) {
                log.error("Error calling storage client: {}", e.getMessage());
            }
        }

        return null;
    }

    /**
     * Gets ExternalClient keys.
     *
     * @return list of String.
     */
    public static List<String> getExternalKeys() {
        return new ArrayList<>(externalClients.keySet());
    }
}

package com.mimecast.robin.smtp.security;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Factories;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Standard TLS handshake negociation implementation.
 */
public class DefaultTLSSocket implements TLSSocket {
    private static final Logger log = LogManager.getLogger(DefaultTLSSocket.class);

    /**
     * Socket instance.
     */
    private Socket socket;

    /**
     * Default TLS protocols supported as string array.
     */
    private String[] protocols;

    /**
     * Default TLS cipher suites supported as string array.
     */
    private String[] ciphers;

    /**
     * Sets socket.
     *
     * @param socket Socket instance.
     * @return Self.
     */
    @Override
    public TLSSocket setSocket(Socket socket) {
        this.socket = socket;
        return this;
    }

    /**
     * Sets TLS protocols supported.
     *
     * @param protocols Protocols list.
     * @return Self.
     */
    @Override
    public TLSSocket setProtocols(String[] protocols) {
        if (protocols != null) {
            this.protocols = protocols;
        }
        return this;
    }

    /**
     * Sets TLS ciphers supported.
     *
     * @param ciphers Cipher suites list.
     * @return Self.
     */
    @Override
    public TLSSocket setCiphers(String[] ciphers) {
        if (protocols != null) {
            this.ciphers = ciphers;
        }
        return this;
    }

    /**
     * Enable encryption for the given socket.
     *
     * @param client True if in client mode.
     * @return SSLSocket instance.
     * @throws IOException              Unable to read.
     * @throws GeneralSecurityException Problems with TrustManager or KeyManager.
     */
    @Override
    public SSLSocket startTLS(boolean client) throws IOException, GeneralSecurityException {
        if (socket == null) {
            throw new IOException("Socket not defined");
        }

        // Trust manager.
        TrustManager[] tm = new TrustManager[]{Factories.getTrustManager()};

        // Key manager X.509.
        KeyManager[] km = null;
        if (!client) {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            KeyStore ks = KeyStore.getInstance("JKS");

            // Load keystore.
            ks.load(getKeyStore(), getKeyStorePassword());
            kmf.init(ks, getKeyStorePassword());

            km = kmf.getKeyManagers();
        }

        // Get the default SSLSocketFactory.
        @SuppressWarnings("squid:S4423")
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(km, tm, new SecureRandom());
        SSLSocketFactory sf = sc.getSocketFactory();

        // Wrap 'socket' from above in a TLS socket.
        InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
        @SuppressWarnings("squid:S2095")
        SSLSocket sslSocket = (SSLSocket) sf.createSocket(socket, remoteAddress.getHostName(), socket.getPort(), true);

        // We are a client.
        sslSocket.setUseClientMode(client);

        // Allowed TLS protocols and supported cipher suites.
        if (client) {
            sslSocket.setEnabledProtocols(getEnabledProtocols(sslSocket));
            sslSocket.setEnabledCipherSuites(getEnabledCipherSuites(sslSocket));
        }

        // Make a friend!
        sslSocket.startHandshake();
        log.debug("Handshake done with: {} / {}.", sslSocket.getSession().getProtocol(), sslSocket.getSession().getCipherSuite());

        return sslSocket;
    }

    /**
     * Gets default protocols or enabled ones from configured list.
     *
     * @param sslSocket SSLSocket instance.
     * @return Protocols list.
     */
    @Override
    public String[] getEnabledProtocols(SSLSocket sslSocket) {
        List<String> defaultProtocols = Arrays.asList(sslSocket.getEnabledProtocols());

        if (protocols != null && protocols.length > 0) {
            log.debug("Configured protocols: {}", String.join(", ", protocols));

            List<String> supportedProtocols = new ArrayList<>();
            for (String protocol : protocols) {
                if (defaultProtocols.contains(protocol)) {
                    supportedProtocols.add(protocol);
                }
            }
            log.trace("Supported protocols: {}", String.join(", ", supportedProtocols));

            return supportedProtocols.toArray(new String[0]);
        }

        return defaultProtocols.toArray(new String[0]);
    }

    /**
     * Gets default cipher suites or enabled ones from configured list.
     *
     * @param sslSocket SSLSocket instance.
     * @return Cipher suites list.
     */
    @Override
    public String[] getEnabledCipherSuites(SSLSocket sslSocket) {
        List<String> defaultCipherSuites = Arrays.asList(sslSocket.getEnabledCipherSuites());

        if (ciphers != null && ciphers.length > 0) {
            log.debug("Configured cipher suites: {}", String.join(", ", ciphers));
            List<String> supportedCipherSuites = new ArrayList<>();
            for (String cipherSuite : ciphers) {
                if (defaultCipherSuites.contains(cipherSuite)) {
                    supportedCipherSuites.add(cipherSuite);
                }
            }
            log.trace("Supported cipher suites: {}", String.join(", ", supportedCipherSuites));

            return supportedCipherSuites.toArray(new String[0]);
        }

        return defaultCipherSuites.toArray(new String[0]);
    }

    /**
     * Gets keystore.
     *
     * @return Keystore.
     */
    private FileInputStream getKeyStore() {
        try {
            String path = Config.getProperties().getStringProperty("javax.net.ssl.keyStore");
            if (StringUtils.isNotBlank(path)) {
                return new FileInputStream(path);
            }
        } catch (FileNotFoundException e) {
            log.error("Error getting keystore.");
        }
        return null;
    }

    /**
     * Gets keystore password
     *
     * @return Password.
     */
    private char[] getKeyStorePassword() {
        String password = Config.getProperties().getStringProperty("javax.net.ssl.keyStorePassword");
        return StringUtils.isNotBlank(password) ? password.toCharArray() : "".toCharArray();
    }
}

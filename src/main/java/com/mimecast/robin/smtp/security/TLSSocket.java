package com.mimecast.robin.smtp.security;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;

/**
 * TLS socket.
 */
public interface TLSSocket {

    /**
     * Sets socket.
     *
     * @param socket Socket instance.
     * @return Self.
     */
    TLSSocket setSocket(Socket socket);

    /**
     * Sets TLS protocols supported.
     *
     * @param protocols Protocols list.
     * @return Self.
     */
    TLSSocket setProtocols(String[] protocols);

    /**
     * Sets TLS ciphers supported.
     *
     * @param ciphers Cipher suites list.
     * @return Self.
     */
    TLSSocket setCiphers(String[] ciphers);

    /**
     * Enable encryption for the given socket.
     *
     * @param client True if in client mode.
     * @return SSLSocket instance.
     * @throws IOException              Unable to read.
     * @throws GeneralSecurityException Problems with TrustManager or KeyManager.
     */
    SSLSocket startTLS(boolean client) throws IOException, GeneralSecurityException;

    /**
     * Gets default protocols or enabled ones from configured list.
     *
     * @param sslSocket SSLSocket instance.
     * @return Protocols list.
     */
    String[] getEnabledProtocols(SSLSocket sslSocket);

    /**
     * Gets default cipher suites or enabled ones from configured list.
     *
     * @param sslSocket SSLSocket instance.
     * @return Cipher suites list.
     */
    String[] getEnabledCipherSuites(SSLSocket sslSocket);
}

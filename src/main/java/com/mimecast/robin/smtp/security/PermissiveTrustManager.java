package com.mimecast.robin.smtp.security;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;

/**
 * All trusting manager.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
@SuppressWarnings("all")
public class PermissiveTrustManager implements X509TrustManager {

    /**
     * Is client trusted.
     *
     * @param chain Peer certificate chain.
     * @return Boolean.
     */
    public boolean isClientTrusted(X509Certificate[] chain) {
        return true;
    }

    /**
     * Is host trusted.
     *
     * @param chain Peer certificate chain.
     * @return Boolean.
     */
    public boolean isHostTrusted(X509Certificate[] chain) {
        return true;
    }

    /**
     * Check if client is trusted.
     *
     * @param chain Peer certificate chain.
     * @param authType Key exchange algorithm used.
     * @throws CertificateException If the certificate chain is not trusted.
     */
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        // The purpose of this is to trust everything.
    }

    /**
     * Check if server is trusted.
     *
     * @param chain Peer certificate chain.
     * @param authType Key exchange algorithm used.
     * @throws CertificateException If the certificate chain is not trusted.
     */
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        // The purpose of this is to trust everything.
    }

    /**
     * Gets accepted issuers.
     *
     * @return X509Certificate array.
     */
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}

package com.mimecast.robin.smtp.security;

import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.security.cert.CertificateException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissiveTrustManagerTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void use() throws CertificateException {
        PermissiveTrustManager tm = new PermissiveTrustManager();
        tm.checkClientTrusted(null, null);
        tm.checkServerTrusted(null, null);
        assertTrue(tm.isClientTrusted(null));
        assertTrue(tm.isHostTrusted(null));
    }
}

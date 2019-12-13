package com.mimecast.robin.config.client;

import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CaseConfigTest {

    private static CaseConfig caseConfig;

    @BeforeAll
    static void before() throws IOException, ConfigurationException {
        Foundation.init("src/test/resources/");

        caseConfig = new CaseConfig("src/test/resources/case.json");
    }

    @Test
    void getMx() {
        assertEquals("example.net", caseConfig.getMx().get(0));
    }

    @Test
    void getPort() {
        assertEquals(465, caseConfig.getPort());
    }

    @Test
    void isAuth() {
        assertTrue(caseConfig.isAuth());
    }

    @Test
    void getUser() {
        assertEquals("tony@example.com", caseConfig.getUser());
    }

    @Test
    void getPass() {
        assertEquals("giveHerTheRing", caseConfig.getPass());
    }

    @Test
    void isTls() {
        assertTrue(caseConfig.isTls());
    }

    @Test
    void isAuthTls() {
        assertTrue(caseConfig.isAuthTls());
    }

    @Test
    void getProtocols() {
        assertTrue(String.join(", ", caseConfig.getProtocols()).contains("TLSv1.1"));
        assertTrue(String.join(", ", caseConfig.getProtocols()).contains("TLSv1.2"));
    }

    @Test
    void getCiphers() {
        assertTrue(String.join(", ", caseConfig.getCiphers()).contains("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384"));
        assertTrue(String.join(", ", caseConfig.getCiphers()).contains("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384"));
        assertTrue(String.join(", ", caseConfig.getCiphers()).contains("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"));
        assertTrue(String.join(", ", caseConfig.getCiphers()).contains("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384"));
    }

    @Test
    void getEhlo() {
        assertEquals("example.com", caseConfig.getEhlo());
    }

    @Test
    void getMail() {
        assertEquals("tony@example.com", caseConfig.getMail());
    }

    @Test
    void getRcpt() {
        assertTrue(String.join(", ", caseConfig.getRcpt()).contains("pepper@example.com"));
    }

    @Test
    void getEnvelopes() {
        assertEquals(2, caseConfig.getEnvelopes().size());
    }

    @Test
    void getAssertions() {
        // Tested in AssertConfigTest & AssertMtaConfigTest
        assertNotNull(caseConfig.getAssertions());
    }
}

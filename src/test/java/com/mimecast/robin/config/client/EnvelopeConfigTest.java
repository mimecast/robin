package com.mimecast.robin.config.client;

import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnvelopeConfigTest {

    private static EnvelopeConfig envelopeConfig1;
    private static EnvelopeConfig envelopeConfig2;

    @BeforeAll
    static void before() throws IOException, ConfigurationException {
        Foundation.init("src/test/resources/");

        CaseConfig caseConfig = new CaseConfig("src/test/resources/case.json5");
        envelopeConfig1 = caseConfig.getEnvelopes().get(0);
        envelopeConfig2 = caseConfig.getEnvelopes().get(1);
    }

    @Test
    void getMail() {
        assertEquals("tony@example.com", envelopeConfig1.getMail());

        assertEquals("", envelopeConfig2.getMail());
    }

    @Test
    void getRcpt() {
        assertTrue(String.join(", ", envelopeConfig1.getRcpt()).contains("pepper@example.com"));
        assertTrue(String.join(", ", envelopeConfig1.getRcpt()).contains("happy@example.com"));

        assertTrue(String.join(", ", envelopeConfig2.getRcpt()).contains("journalling@example.com"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getHeaders() {
        Map<String, Object> headers = envelopeConfig1.getHeaders();
        assertEquals("{$mail}", headers.get("from"));
        assertEquals("{$rcpt}", ((List<String>) headers.get("to")).get(0));

        headers = envelopeConfig2.getHeaders();
        assertEquals("tony@example.com", headers.get("from"));
        assertEquals("pepper@example.com", ((List<String>) headers.get("to")).get(0));
    }

    @Test
    void getChunkSize() {
        assertEquals(2048, envelopeConfig1.getChunkSize());

        assertEquals(0, envelopeConfig2.getChunkSize());
    }

    @Test
    void isChunkBdat() {
        assertTrue(envelopeConfig1.isChunkBdat());

        assertFalse(envelopeConfig2.isChunkBdat());
    }

    @Test
    void isChunkWrite() {
        assertTrue(envelopeConfig1.isChunkWrite());

        assertFalse(envelopeConfig2.isChunkWrite());
    }

    @Test
    void getFile() {
        assertEquals("src/test/resources/lipsum.eml", envelopeConfig1.getFile());

        assertNull(envelopeConfig2.getFile());
    }

    @Test
    void getFolder() {
        assertEquals("src/test/resources/", envelopeConfig1.getFolder());

        assertNull(envelopeConfig2.getFolder());
    }

    @Test
    void getSubject() {
        assertNull(envelopeConfig1.getSubject());

        assertEquals("Lost in space", envelopeConfig2.getSubject());
    }

    @Test
    void getMessage() {
        assertNull(envelopeConfig1.getMessage());

        assertEquals("Rescue me!", envelopeConfig2.getMessage());
    }

    @Test
    void getAssertions() {
        // Tested in AssertConfigTest & AssertMtaConfigTest
        assertNotNull(envelopeConfig1.getAssertions());

        assertNotNull(envelopeConfig2.getAssertions());
    }
}

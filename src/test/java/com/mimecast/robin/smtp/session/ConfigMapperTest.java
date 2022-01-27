package com.mimecast.robin.smtp.session;

import com.mimecast.robin.config.ConfigMapper;
import com.mimecast.robin.config.client.CaseConfig;
import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.MessageEnvelope;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigMapperTest {

    private static CaseConfig caseConfig;

    @BeforeAll
    static void before() throws IOException, ConfigurationException {
        Foundation.init("src/test/resources/");

        caseConfig = new CaseConfig("src/test/resources/mapper.json5");
    }

    @Test
    void mapTo() {
        Session session = new Session();
        new ConfigMapper(caseConfig).mapTo(session);

        assertEquals(30, session.getTimeout());
        assertEquals("example.net", session.getMx().get(0));
        assertEquals(465, session.getPort());
        assertTrue(session.isTls());
        assertFalse(session.isAuthBeforeTls());
        assertEquals("TLSv1.1, TLSv1.2", String.join(", ", session.getProtocols()));
        assertEquals("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384, " +
                "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384, " +
                "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, " +
                "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", String.join(", ", session.getCiphers()));
        assertEquals("example.com", session.getEhlo());
        assertTrue(session.isAuth());
        assertEquals("tony@example.com", session.getUsername());
        assertEquals("giveHerTheRing", session.getPassword());

        List<MessageEnvelope> envelopes = session.getEnvelopes();
        assertEquals("tony@example.com", envelopes.get(0).getMail());
        assertEquals("pepper@example.com", envelopes.get(0).getRcpts().get(0));
        assertEquals("happy@example.com", envelopes.get(0).getRcpts().get(1));

        assertEquals("tony@example.com", envelopes.get(1).getMail());
        assertEquals("pepper@example.com", envelopes.get(1).getRcpts().get(0));
    }
}

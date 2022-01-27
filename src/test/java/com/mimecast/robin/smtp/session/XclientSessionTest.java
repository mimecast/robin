package com.mimecast.robin.smtp.session;

import com.mimecast.robin.config.client.CaseConfig;
import com.mimecast.robin.main.Factories;
import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XclientSessionTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void map() throws IOException {
        XclientSession session = (XclientSession) Factories.getSession();
        session.map(new CaseConfig("src/test/resources/case.json5"));

        assertEquals("example.com", session.getXclient().get("name"));
        assertEquals("example.net", session.getXclient().get("helo"));
        assertEquals("127.0.0.10", session.getXclient().get("addr"));
    }
}

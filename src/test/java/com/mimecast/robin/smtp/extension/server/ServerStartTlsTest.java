package com.mimecast.robin.smtp.extension.server;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import com.mimecast.robin.smtp.verb.Verb;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServerStartTlsTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void getAdvert() {
        ServerStartTls tls = new ServerStartTls();
        assertEquals("STARTTLS", tls.getAdvert());
    }

    @Test
    void process() {
        StringBuilder stringBuilder = new StringBuilder();
        ConnectionMock connection = new ConnectionMock(stringBuilder);

        Verb verb = new Verb("STARTTLS");

        // Testing exception as the flow is covered by ClientStartTlsTest.java.
        assertThrows(IOException.class, () -> new ServerStartTls().process(connection, verb));
    }
}
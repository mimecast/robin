package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.config.server.ServerConfig;
import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Factories;
import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.SmtpListener;
import com.mimecast.robin.smtp.connection.Connection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ClientStartTlsTest {

    private static Thread thread;

    @BeforeAll
    @SuppressWarnings("squid:S2925")
    static void before() throws InterruptedException, ConfigurationException {
        Foundation.init("src/test/resources/");

        ServerConfig config = Config.getServer();

        thread = new Thread(() -> {
            try {
                System.setProperty("javax.net.ssl.keyStore", config.getKeyStore());
                System.setProperty("javax.net.ssl.keyStorePassword", config.getKeyStorePassword());
                new SmtpListener(10025, config.getBacklog(), "localhost");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        thread.start();
        Thread.sleep(100);
    }

    @AfterAll
    static void after() {
        try {
            thread.interrupt();
        } catch (Exception ignored) {
        }
    }

    @Test
    void process() throws IOException {
        Connection connection = new Connection(Factories.getSession());
        connection.getSession().setMx(Collections.singletonList("localhost"));
        connection.getSession().setPort(10025);
        connection.getSession().setTls(true);
        connection.getSession().setEhloTls(true);
        connection.connect();

        // Read welcome message.
        assertEquals("220", connection.getSession().getSessionTransactionList().getLast("SMTP").getResponseCode());

        ClientStartTls startTls = new ClientStartTls();
        boolean process = startTls.process(connection);

        // Read STARTTLS response.
        assertTrue(process);
        assertFalse(connection.getSession().getSessionTransactionList().getLast("STARTTLS").isError());
        assertFalse(connection.getSession().getSessionTransactionList().getLast("TLS").isError());
    }
}

package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.config.server.ServerConfig;
import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.SmtpListener;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import com.mimecast.robin.smtp.io.LineInputStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;

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
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("220 Go\r\n");
        ConnectionMock connection = new ConnectionMock(stringBuilder);
        connection.getSession().setMx("localhost");
        connection.getSession().setPort(10025);
        connection.getSession().setTls(true);
        connection.getSession().setEhloTls(true);
        connection.superConnect();

        // Handle the real socket read and writes since streams are mocked.
        String read;

        // Read welcome message.
        read = socketRead(connection);
        assertTrue(read.startsWith("220"));

        sendStartTls(connection);

        // Read STARTTLS response.
        read = socketRead(connection);
        assertTrue(read.startsWith("220"));

        ClientStartTls startTls = new ClientStartTls();
        boolean process = startTls.process(connection);

        assertTrue(process);
        assertFalse(connection.getSessionTransactionList().getLast("TLS").isError());

        connection.parseLines();
        assertEquals("STARTTLS\r\n", connection.getLine(1));
    }

    private String socketRead(ConnectionMock connection) throws IOException {
        StringBuilder received = new StringBuilder();

        byte[] read;
        while ((read = new LineInputStream(connection.getSocket().getInputStream()).readLine()) != null) {
            received.append(new String(read));
            if (read.length < 4 || read[3] != 45) break;
        }

        return received.toString();
    }

    private void sendStartTls(ConnectionMock connection) throws IOException {
        connection.getSocket().getOutputStream().write("STARTTLS\r\n".getBytes());
    }
}

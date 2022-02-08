package com.mimecast.robin.storage;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.session.Session;
import com.mimecast.robin.util.PathUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStorageClientTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void simple() {
        LocalStorageClient localStorageClient = new LocalStorageClient().setExtension("dat");

        assertTrue(localStorageClient.getToken().contains("/tmp/store/"));
        assertTrue(localStorageClient.getToken().contains(new SimpleDateFormat("yyyyMMdd", Locale.UK).format(new Date()) + "."));
        assertTrue(localStorageClient.getToken().contains(".dat"));
    }

    @Test
    void conenction() throws AddressException {
        Connection connection = new Connection(new Session().addRcpt(new InternetAddress("vmarian@mimecast.com")));
        LocalStorageClient localStorageClient = new LocalStorageClient().setExtension("dat").setConnection(connection);

        assertTrue(localStorageClient.getToken().contains("/tmp/store/mimecast.com/vmarian/"));
        assertTrue(localStorageClient.getToken().contains(new SimpleDateFormat("yyyyMMdd", Locale.UK).format(new Date()) + "."));
        assertTrue(localStorageClient.getToken().contains(".dat"));
    }

    @Test
    void stream() throws AddressException, IOException {
        Connection connection = new Connection(new Session().addRcpt(new InternetAddress("vmarian@mimecast.com")));
        LocalStorageClient localStorageClient = new LocalStorageClient().setExtension("eml").setConnection(connection);

        String content = "Mime-Version: 1.0\r\n";
        localStorageClient.getStream().write(content.getBytes());
        localStorageClient.save();

        assertEquals(content, PathUtils.readFile(localStorageClient.getToken(), Charset.defaultCharset()));
        assertTrue(new File(localStorageClient.getToken()).delete());
    }

    @Test
    void filename() throws AddressException, IOException {
        Connection connection = new Connection(new Session().addRcpt(new InternetAddress("vmarian@mimecast.com")));
        LocalStorageClient localStorageClient = new LocalStorageClient().setExtension("dat").setConnection(connection);

        String content = "Mime-Version: 1.0\r\n" +
                "X-Robin-Filename: robin.eml\r\n" +
                "\r\n";
        localStorageClient.getStream().write(content.getBytes());

        assertTrue(localStorageClient.getToken().endsWith(".dat"));

        localStorageClient.save();

        assertTrue(localStorageClient.getToken().endsWith("robin.eml"));
        assertEquals(content, PathUtils.readFile(localStorageClient.getToken(), Charset.defaultCharset()));
        assertTrue(new File(localStorageClient.getToken()).delete());
    }
}

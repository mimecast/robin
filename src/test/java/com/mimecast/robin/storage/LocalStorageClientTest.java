package com.mimecast.robin.storage;

import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.session.Session;
import com.mimecast.robin.util.PathUtils;
import org.junit.jupiter.api.Test;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStorageClientTest {

    @Test
    void simple() {
        LocalStorageClient localStorageClient = new LocalStorageClient("dat");

        assertTrue(localStorageClient.getToken().contains("/tmp/store/"));
        assertTrue(localStorageClient.getToken().contains(new SimpleDateFormat("yyyyMMdd", Locale.UK).format(new Date()) + "."));
        assertTrue(localStorageClient.getToken().contains(".dat"));
    }

    @Test
    void conenction() throws AddressException {
        Connection connection = new Connection(new Session().addRcpt(new InternetAddress("vmarian@mimecast.com")));
        LocalStorageClient localStorageClient = new LocalStorageClient("dat").setConnection(connection);

        assertTrue(localStorageClient.getToken().contains("/tmp/store/mimecast.com/vmarian/"));
        assertTrue(localStorageClient.getToken().contains(new SimpleDateFormat("yyyyMMdd", Locale.UK).format(new Date()) + "."));
        assertTrue(localStorageClient.getToken().contains(".dat"));
    }

    @Test
    void stream() throws AddressException, IOException {
        Connection connection = new Connection(new Session().addRcpt(new InternetAddress("vmarian@mimecast.com")));
        LocalStorageClient localStorageClient = new LocalStorageClient("eml").setConnection(connection);

        String content = "Mime-Version: 1.0\r\n";
        localStorageClient.getStream().write(content.getBytes());
        localStorageClient.save();

        assertEquals(content, PathUtils.readFile(localStorageClient.getToken(), Charset.defaultCharset()));
        assertTrue(new File(localStorageClient.getToken()).delete());
    }
}

package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import com.mimecast.robin.smtp.session.XclientSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientXclientTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void process() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("220 Go\r\n");
        ConnectionMock connection = new ConnectionMock(stringBuilder);

        Map<String, String> map = new HashMap<>();
        map.put("name", "example.com");
        map.put("helo", "example.com");
        map.put("addr", "127.0.0.1");
        ((XclientSession) connection.getSession()).setXclient(map);

        ClientXclient xclient = new ClientXclient();
        boolean process = xclient.process(connection);

        assertTrue(process);
        assertEquals("220 Go", connection.getSession().getSessionTransactionList().getLast("XCLIENT").getResponse());

        connection.parseLines();
        assertEquals("XCLIENT helo=example.com name=example.com addr=127.0.0.1\r\n", connection.getLine(1));
    }
}

package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientQuitTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void process() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("250 OK\r\n");
        ConnectionMock connection = new ConnectionMock(stringBuilder);

        ClientQuit quit = new ClientQuit();
        boolean process = quit.process(connection);

        assertTrue(process);

        connection.parseLines();
        assertEquals("QUIT\r\n", connection.getLine(1));
    }
}

package com.mimecast.robin.smtp.extension.server;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import com.mimecast.robin.smtp.verb.Verb;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ServerQuitTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void process() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        ConnectionMock connection = new ConnectionMock(stringBuilder);

        Verb verb = new Verb("QUIT");

        ServerQuit quit = new ServerQuit();
        boolean process = quit.process(connection, verb);

        assertFalse(process);

        connection.parseLines();
        assertEquals("221 2.0.0 Closing connection\r\n", connection.getLine(1));
    }
}

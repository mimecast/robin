package com.mimecast.robin.smtp.extension.server;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import com.mimecast.robin.smtp.verb.Verb;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerHelpTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void process() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        ConnectionMock connection = new ConnectionMock(stringBuilder);

        Verb verb = new Verb("HELP");

        ServerHelp help = new ServerHelp();
        boolean process = help.process(connection, verb);

        assertTrue(process);

        String out = connection.getOutput();
        assertTrue(out.contains("HELO"));
        assertTrue(out.contains("EHLO"));
        assertTrue(out.contains("STARTTLS"));
        assertTrue(out.contains("AUTH"));
        assertTrue(out.contains("MAIL"));
        assertTrue(out.contains("RCPT"));
        assertTrue(out.contains("DATA"));
        assertTrue(out.contains("BDAT"));
        assertTrue(out.contains("RSET"));
        assertTrue(out.contains("HELP"));
        assertTrue(out.contains("QUIT"));
    }
}

package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.main.Extensions;
import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientHelpTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void process() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("214 ");
        stringBuilder.append(Extensions.getHelp());
        stringBuilder.append("\r\n");
        ConnectionMock connection = new ConnectionMock(stringBuilder);

        ClientHelp help = new ClientHelp();
        boolean process = help.process(connection);

        assertTrue(process);

        connection.parseLines();
        assertTrue(connection.getLine(1).startsWith("HELP"));
        
        String response = connection.getSession().getSessionTransactionList().getLast("HELP").getResponse();
        assertTrue(response.contains("HELO"));
        assertTrue(response.contains("EHLO"));
        assertTrue(response.contains("STARTTLS"));
        assertTrue(response.contains("AUTH"));
        assertTrue(response.contains("MAIL"));
        assertTrue(response.contains("RCPT"));
        assertTrue(response.contains("DATA"));
        assertTrue(response.contains("BDAT"));
        assertTrue(response.contains("RSET"));
        assertTrue(response.contains("HELP"));
        assertTrue(response.contains("QUIT"));
    }
}

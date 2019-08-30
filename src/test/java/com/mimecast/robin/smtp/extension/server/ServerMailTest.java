package com.mimecast.robin.smtp.extension.server;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import com.mimecast.robin.smtp.verb.Verb;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerMailTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void process() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        ConnectionMock connection = new ConnectionMock(stringBuilder);

        Verb verb = new Verb("MAIL FROM: <tony@example.com> SIZE=42 BODY=BINARYMIME " +
                "NOTIFY=FAILURE,DELAY ORCPT=rfc822;pepper@example.com RET=HDRS ENVID=QQ314159");

        ServerMail mail = new ServerMail();
        boolean process = mail.process(connection, verb);

        assertTrue(process);
        assertEquals("tony@example.com", mail.getAddress().getAddress());
        assertEquals("tony@example.com", connection.getSession().getMail().getAddress());
        assertEquals(42, mail.getSize());
        assertEquals("BINARYMIME", mail.getBody());
        assertEquals("[FAILURE, DELAY]", Arrays.toString(mail.getNotify()));
        assertEquals("pepper@example.com", mail.getORcpt().getAddress());
        assertEquals("HDRS", mail.getRet());
        assertEquals("QQ314159", mail.getEnvId());
    }
}

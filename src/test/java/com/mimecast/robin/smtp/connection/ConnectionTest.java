package com.mimecast.robin.smtp.connection;

import com.mimecast.robin.config.server.ScenarioConfig;
import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    private ConnectionMock getConnection(StringBuilder stringBuilder) {
        ConnectionMock connection = new ConnectionMock(stringBuilder);
        connection.getSession().setRdns("example.com");
        connection.getSession().setFriendRdns("example.net");
        connection.getSession().setFriendAddr("127.0.0.1");

        return connection;
    }

    @Test
    void scenarios() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("EHLO helo.com\r\n");
        stringBuilder.append("HELO example.com\r\n");
        stringBuilder.append("QUIT\r\n");

        ConnectionMock connection = getConnection(stringBuilder);
        assertTrue(connection.getScenario().isPresent());
        assertEquals("252 I think I know this user", connection.getScenario().get().getRcpt().get(0).get("response"));

        connection.setScenario(new ScenarioConfig(new HashMap<String, String>() {{
            put("ehlo", "501 Not talking to you");
        }}));
        assertTrue(connection.getScenario().isPresent());
        assertEquals("501 Not talking to you", connection.getScenario().get().getEhlo());
    }
}

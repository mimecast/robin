package com.mimecast.robin.assertion.client.humio;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.config.assertion.external.logs.LogsExternalClientConfig;
import com.mimecast.robin.main.Config;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import com.mimecast.robin.smtp.transaction.EnvelopeTransactionList;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class HumioClientTest {

    @Test
    void run() throws AssertException {
        Config.getProperties().getMap().put("uid.pattern", "\\s\\[([a-z0-9\\-_]+)\\.([a-z]+)([0-9]+)?]");

        Map<String, Object> config = Stream.of(
                new AbstractMap.SimpleEntry<>("type", "humio"),
                new AbstractMap.SimpleEntry<>("wait", 0),
                new AbstractMap.SimpleEntry<>("delay", 0),
                new AbstractMap.SimpleEntry<>("retry", 0),
                new AbstractMap.SimpleEntry<>("verify", Collections.singletonList("QUIT")),
                new AbstractMap.SimpleEntry<>("match", Arrays.asList(
                        Arrays.asList("MAIL FROM", "<tony@example.com>", "SIZE=294"),
                        Arrays.asList("RCPT TO", "<pepper@example.com>")
                )),
                new AbstractMap.SimpleEntry<>("refuse", Collections.singletonList(
                        Collections.singletonList("java.lang.NullPointerException")
                ))
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("220 example.com ESMTP\r\n");
        stringBuilder.append("250-smtp.example.com at your service, [127.0.0.1]\r\n" +
                "250 HELP\r\n");
        stringBuilder.append("250 2.1.0 Sender OK\r\n");
        stringBuilder.append("250 2.1.5 Recipient OK\r\n");
        stringBuilder.append("354 Ready and willing\r\n");
        stringBuilder.append("250 2.0.0 Received OK\r\n");
        stringBuilder.append("221 2.0.0 Closing connection\r\n");

        EnvelopeTransactionList envelopeTransactionList = new EnvelopeTransactionList();
        envelopeTransactionList.addTransaction("MAIL", "250 Sender OK", false);

        Connection connection = ConnectionMock.getConnection(stringBuilder)
                .setServer("example.com");
        connection.getSessionTransactionList().getEnvelopes().add(envelopeTransactionList);

        new HumioExternalClientMock()
                .setConnection(connection)
                .setConfig(new LogsExternalClientConfig(config))
                .setTransactionId(0)
                .run();

    }
}
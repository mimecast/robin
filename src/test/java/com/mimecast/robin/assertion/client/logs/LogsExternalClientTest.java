package com.mimecast.robin.assertion.client.logs;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.config.assertion.external.logs.LogsExternalClientConfig;
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

class LogsExternalClientTest {

    @Test
    void uid() throws AssertException {
        Map<String, Object> logsExternalClientConfig = Stream.of(
                new AbstractMap.SimpleEntry<>("type", "logs"),
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
        stringBuilder.append("250-smtp.example.com at your service, [rescueMe.local]\r\n" +
                "250 HELP\r\n");
        stringBuilder.append("250 2.1.0 Sender OK\r\n");
        stringBuilder.append("250 2.1.5 Recipient OK\r\n");
        stringBuilder.append("354 Ready and willing\r\n");
        stringBuilder.append("250 2.0.0 Received OK\r\n");
        stringBuilder.append("221 2.0.0 Closing connection\r\n");

        EnvelopeTransactionList envelopeTransactionList = new EnvelopeTransactionList();
        envelopeTransactionList.addTransaction("MAIL", "250 ok [rescueMe]");

        Connection connection = ConnectionMock.getConnection(stringBuilder)
                .setServer("space.com");
        connection.getSessionTransactionList().getEnvelopes().add(envelopeTransactionList);

        new LogsExternalClientMock()
                .setConnection(connection)
                .setConfig(new LogsExternalClientConfig(logsExternalClientConfig))
                .setTransactionId(0)
                .run();
    }
}
package com.mimecast.robin.assertion;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mimecast.robin.config.assertion.AssertConfig;
import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.session.Session;
import com.mimecast.robin.smtp.transaction.EnvelopeTransactionList;
import com.mimecast.robin.smtp.transaction.SessionTransactionList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class AssertTest {

    private static AssertConfig assertConfig;

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");

        String json =
                "{\n" +
                        "  \"envelopes\": [\n" +
                        "    {\n" +
                        "      \"assertions\": {\n" +
                        "        \"smtp\": [\n" +
                        "          [\"MAIL\", \"250 Sender OK\"],\n" +
                        "          [\"RCPT\", \"250 Recipient OK\"],\n" +
                        "          [\"DATA\", \"^250\"],\n" +
                        "          [\"DATA\", \"Received OK$\"]\n" +
                        "        ]\n" +
                        "      }\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"smtp\": [\n" +
                        "    [\"SMTP\", \"^220\"]\n" +
                        "  ]\n" +
                        "}";

        Map<String, Object> map = new Gson().fromJson(json, new TypeToken<HashMap<String, Object>>() {
        }.getType());
        assertConfig = new AssertConfig(map);
    }

    @Test
    @SuppressWarnings("java:S2699")
    void session() throws AssertException {
        SessionTransactionList sessionTransactionList = new SessionTransactionList();
        sessionTransactionList.addTransaction("SMTP", "220 example.com ESMTP", false);

        Session session = new Session();
        session.addAssertions(assertConfig);

        new Assert(new Connection(session, sessionTransactionList)).run();
    }

    @Test
    @SuppressWarnings({"rawtypes", "java:S2699"})
    void message() throws AssertException {
        SessionTransactionList sessionTransactionList = new SessionTransactionList();

        EnvelopeTransactionList envelopeTransactionList = new EnvelopeTransactionList();
        envelopeTransactionList.addTransaction("MAIL", "250 Sender OK [7qGJZ4oRNkWJPsu_7ug1nw.localhost]", false);
        envelopeTransactionList.addTransaction("RCPT", "250 Recipient OK", false);
        envelopeTransactionList.addTransaction("DATA", "250 Received OK", false);
        sessionTransactionList.addEnvelope(envelopeTransactionList);

        MessageEnvelope envelope = new MessageEnvelope();
        envelope.setAssertions(new AssertConfig((Map) ((Map) ((List) assertConfig.getMap().get("envelopes")).get(0)).get("assertions")));

        Session session = new Session();
        session.addEnvelope(envelope);

        new Assert(new Connection(session, sessionTransactionList)).run();
    }

    @Test
    @SuppressWarnings("java:S2699")
    void blank() throws AssertException {
        String json = "{}";

        Map<String, Object> map = new Gson().fromJson(json, new TypeToken<HashMap<String, Object>>() {
        }.getType());
        AssertConfig assertConfig = new AssertConfig(map);

        Session session = new Session();
        session.addAssertions(assertConfig);

        new Assert(new Connection(session, new SessionTransactionList())).run();
    }
}

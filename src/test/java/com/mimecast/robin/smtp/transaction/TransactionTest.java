package com.mimecast.robin.smtp.transaction;

import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void withPayload() {
        String command = "EHLO";
        String payload = "MAIL FROM:<tony@example.com>";
        String response = "250 OK";

        Transaction transaction = new Transaction(command).setPayload(payload).setResponse(response).setError(false);

        assertEquals(payload, transaction.getPayload());
        assertEquals(response, transaction.getResponse());
        assertEquals("250", transaction.getResponseCode());
        assertEquals("OK", transaction.getResponseString());
        assertFalse(transaction.isError());
        assertEquals(command + "> " + payload, transaction.toString());

    }

    @Test
    void withoutPayload() {
        String command = "EHLO";
        String response = "250 OK";

        Transaction transaction = new Transaction(command).setResponse(response).setError(false);

        assertNull(transaction.getPayload());
        assertEquals(response, transaction.getResponse());
        assertEquals("250", transaction.getResponseCode());
        assertEquals("OK", transaction.getResponseString());
        assertFalse(transaction.isError());
        assertEquals(command, transaction.toString());

    }
}

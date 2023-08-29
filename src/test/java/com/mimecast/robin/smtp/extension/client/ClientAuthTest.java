package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.auth.DigestMD5Client;
import com.mimecast.robin.smtp.auth.InstanceDigestCache;
import com.mimecast.robin.smtp.auth.NotRandom;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ClientAuthTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    static class ClientAuthMock extends ClientAuth {
        @Override
        protected DigestMD5Client digestMD5ClientFactory() {
            DigestMD5Client client = super.digestMD5ClientFactory();
            client.setRandom(new NotRandom("whatever"));
            client.setDigestDatabase(new InstanceDigestCache());
            return client;
        }
    }

    static class ClientAuthSubsequentMock extends ClientAuth {
        @Override
        protected DigestMD5Client digestMD5ClientFactory() {
            DigestMD5Client client = super.digestMD5ClientFactory();
            client.setRandom(new NotRandom("whatever"));
            return client;
        }
    }

    private ConnectionMock getConnection(StringBuilder stringBuilder) {
        ConnectionMock connection = new ConnectionMock(stringBuilder);
        connection.getSession()
                .setEhlo("example.com")
                .setUsername("tony@example.com")
                .setPassword("giveHerTheRing");
        connection.getSession().setEhloAuth(Arrays.asList("digest-md5", "login", "plain"));
        return connection;
    }

    @Test
    void process() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        ConnectionMock connection = getConnection(stringBuilder);
        connection.getSession().setEhloAuth(new ArrayList<>());

        ClientAuth auth = new ClientAuthMock();
        boolean process = auth.process(connection);

        assertFalse(false);
        assertEquals(0, connection.getSession().getSessionTransactionList().getEnvelopes().size());
        assertEquals(0, connection.getSession().getSessionTransactionList().getTransactions().size());
    }

    @Test
    void processDigestMD5True() throws IOException {
        StringBuilder stringBuilder = new StringBuilder()
                .append("334 cmVhbG09IiIsbm9uY2U9IkdySWZncEw5TGFCbUlZSzEyNnMwZGc9PSIscW9wPSJhdXRoIixjaGFyc2V0PSJ1dGYtOCIsYWxnb3JpdGhtPSJtZDUtc2VzcyI=\r\n")
                .append("334 cnNwYXV0aD0wZmU0ZDQ1MzI3NThjMGI1YzYzMjlkOTBiYjEyY2E4OA==\r\n")
                .append("235 2.7.0 Authorized\r\n");
        ConnectionMock connection = getConnection(stringBuilder);

        ClientAuth auth = new ClientAuthMock();
        boolean process = auth.process(connection);

        assertTrue(process);

        connection.parseLines();
        assertEquals("AUTH DIGEST-MD5\r\n", connection.getLine(1));
        assertEquals("" +
                        "dXNlcm5hbWU9InRvbnlAZXhhbXBsZS5jb20iLHJlYWxtPSIiLHFvcD1hdXRoLG5jPTAwMDAwMDAxLG5v" +
                        "bmNlPSJHcklmZ3BMOUxhQm1JWUsxMjZzMGRnPT0iLGNub25jZT0id2hhdGV2ZXIiLGRpZ2VzdC11cmk9" +
                        "InNtdHAvZXhhbXBsZS5jb20iLHJlc3BvbnNlPTY0ZGU1YWVhZmZhNmU5ZTNkOTc1NDk1MDZhMTRlMzZj" +
                        "\r\n",
                connection.getLine(2));

        assertEquals(1, connection.getSession().getSessionTransactionList().getTransactions().size());
        assertEquals("235 2.7.0 Authorized", connection.getSession().getSessionTransactionList().getLast("AUTH").getResponse());
        assertFalse(connection.getSession().getSessionTransactionList().getLast("AUTH").isError());
    }

    @Test
    void processDigestMD5Subsequent() throws IOException {
        StringBuilder stringBuilder = new StringBuilder()
                .append("334 cmVhbG09IiIsbm9uY2U9IkdySWZncEw5TGFCbUlZSzEyNnMwZGc9PSIscW9wPSJhdXRoIixjaGFyc2V0PSJ1dGYtOCIsYWxnb3JpdGhtPSJtZDUtc2VzcyI=\r\n")
                .append("334 cnNwYXV0aD0wZmU0ZDQ1MzI3NThjMGI1YzYzMjlkOTBiYjEyY2E4OA==\r\n")
                .append("235 2.7.0 Authorized\r\n");
        ConnectionMock connection = getConnection(stringBuilder);

        ClientAuth auth = new ClientAuthSubsequentMock();
        boolean process = auth.process(connection);

        assertTrue(process);

        // SECOND

        stringBuilder = new StringBuilder().append("235 2.7.0 Authorized\r\n");
        connection = getConnection(stringBuilder);

        auth = new ClientAuthSubsequentMock();
        process = auth.process(connection);

        assertTrue(process);

        connection.parseLines();
        assertEquals("AUTH DIGEST-MD5 " +
                        "dXNlcm5hbWU9InRvbnlAZXhhbXBsZS5jb20iLHJlYWxtPSIiLHFvcD1hdXRoLG5jPTAwMDAwMDAyLG5v" +
                        "bmNlPSJHcklmZ3BMOUxhQm1JWUsxMjZzMGRnPT0iLGNub25jZT0id2hhdGV2ZXIiLGRpZ2VzdC11cmk9" +
                        "InNtdHAvZXhhbXBsZS5jb20iLHJlc3BvbnNlPTY0ZGU1YWVhZmZhNmU5ZTNkOTc1NDk1MDZhMTRlMzZj" +
                        "\r\n",
                connection.getLine(1));

        assertEquals(1, connection.getSession().getSessionTransactionList().getTransactions().size());
        assertEquals("235 2.7.0 Authorized", connection.getSession().getSessionTransactionList().getLast("AUTH").getResponse());
        assertFalse(connection.getSession().getSessionTransactionList().getLast("AUTH").isError());
    }

    @Test
    void processDigestMD5FalseChallenge() throws IOException {
        StringBuilder stringBuilder = new StringBuilder()
                .append("504 5.7.4 Unrecognized authentication type\r\n");
        ConnectionMock connection = getConnection(stringBuilder);

        ClientAuth auth = new ClientAuthMock();
        boolean process = auth.process(connection);

        assertFalse(process);

        assertEquals(1, connection.getSession().getSessionTransactionList().getTransactions().size());
        assertEquals("504 5.7.4 Unrecognized authentication type", connection.getSession().getSessionTransactionList().getLast("AUTH").getResponse());
        assertTrue(connection.getSession().getSessionTransactionList().getLast("AUTH").isError());
    }

    @Test
    void processDigestMD5FalseResponse() throws IOException {
        StringBuilder stringBuilder = new StringBuilder()
                .append("334 cmVhbG09IiIsbm9uY2U9IkdySWZncEw5TGFCbUlZSzEyNnMwZGc9PSIscW9wPSJhdXRoIixjaGFyc2V0PSJ1dGYtOCIsYWxnb3JpdGhtPSJtZDUtc2VzcyI=\r\n")
                .append("535 5.7.1 Unauthorized\r\n");
        ConnectionMock connection = getConnection(stringBuilder);

        ClientAuth auth = new ClientAuthMock();
        boolean process = auth.process(connection);

        assertFalse(process);

        assertEquals(1, connection.getSession().getSessionTransactionList().getTransactions().size());
        assertEquals("535 5.7.1 Unauthorized", connection.getSession().getSessionTransactionList().getLast("AUTH").getResponse());
        assertTrue(connection.getSession().getSessionTransactionList().getLast("AUTH").isError());
    }

    @Test
    void processDigestMD5FalseServer() throws IOException {
        StringBuilder stringBuilder = new StringBuilder()
                .append("334 cmVhbG09IiIsbm9uY2U9IkdySWZncEw5TGFCbUlZSzEyNnMwZGc9PSIscW9wPSJhdXRoIixjaGFyc2V0PSJ1dGYtOCIsYWxnb3JpdGhtPSJtZDUtc2VzcyI=\r\n")
                .append("334 cnNwYXV0aD02MzAyNjBjYjg4NTg0YmRjNTczNWU0YzNkY2M3YWIx==\r\n")
                .append("235 2.7.0 Authorized\r\n");
        ConnectionMock connection = getConnection(stringBuilder);

        ClientAuth auth = new ClientAuthMock();
        boolean process = auth.process(connection);

        assertFalse(process);

        connection.parseLines();
        assertEquals("AUTH DIGEST-MD5\r\n", connection.getLine(1));
        assertEquals("" +
                        "dXNlcm5hbWU9InRvbnlAZXhhbXBsZS5jb20iLHJlYWxtPSIiLHFvcD1hdXRoLG5jPTAwMDAwMDAxLG5v" +
                        "bmNlPSJHcklmZ3BMOUxhQm1JWUsxMjZzMGRnPT0iLGNub25jZT0id2hhdGV2ZXIiLGRpZ2VzdC11cmk9" +
                        "InNtdHAvZXhhbXBsZS5jb20iLHJlc3BvbnNlPTY0ZGU1YWVhZmZhNmU5ZTNkOTc1NDk1MDZhMTRlMzZj" +
                        "\r\n",
                connection.getLine(2));

        assertEquals(1, connection.getSession().getSessionTransactionList().getTransactions().size());
        assertFalse(connection.getSession().getSessionTransactionList().getLast("AUTH").isError());
    }

    @Test
    void processLoginOneStep() throws IOException {
        StringBuilder stringBuilder = new StringBuilder()
                .append("235 2.7.0 Authorized\r\n");
        ConnectionMock connection = getConnection(stringBuilder);
        connection.getSession().setEhloAuth(Arrays.asList("login", "plain"));
        connection.getSession().setAuthLoginCombined(true);

        ClientAuth auth = new ClientAuthMock();
        boolean process = auth.process(connection);

        assertTrue(process);

        connection.parseLines();
        assertEquals("AUTH LOGIN dG9ueUBleGFtcGxlLmNvbQ== Z2l2ZUhlclRoZVJpbmc=\r\n", connection.getLine(1));

        assertEquals(1, connection.getSession().getSessionTransactionList().getTransactions().size());
        assertEquals("235 2.7.0 Authorized", connection.getSession().getSessionTransactionList().getLast("AUTH").getResponse());
        assertFalse(connection.getSession().getSessionTransactionList().getLast("AUTH").isError());
    }

    @Test
    void processLoginTwoStep() throws IOException {
        StringBuilder stringBuilder = new StringBuilder()
                .append("334 UGFzc3dvcmQ6\r\n")
                .append("235 2.7.0 Authorized\r\n");
        ConnectionMock connection = getConnection(stringBuilder);
        connection.getSession().setEhloAuth(Arrays.asList("login", "plain"));
        connection.getSession().setAuthLoginCombined(true);

        ClientAuth auth = new ClientAuthMock();
        boolean process = auth.process(connection);

        assertTrue(process);

        connection.parseLines();
        assertEquals("AUTH LOGIN dG9ueUBleGFtcGxlLmNvbQ== Z2l2ZUhlclRoZVJpbmc=\r\n", connection.getLine(1));
        assertEquals("Z2l2ZUhlclRoZVJpbmc=\r\n", connection.getLine(2));

        assertEquals(1, connection.getSession().getSessionTransactionList().getTransactions().size());
        assertEquals("235 2.7.0 Authorized", connection.getSession().getSessionTransactionList().getLast("AUTH").getResponse());
        assertFalse(connection.getSession().getSessionTransactionList().getLast("AUTH").isError());
    }

    @Test
    void processLoginFalse() throws IOException {
        StringBuilder stringBuilder = new StringBuilder()
                .append("535 5.7.1 Unauthorized\r\n");
        ConnectionMock connection = getConnection(stringBuilder);
        connection.getSession().setEhloAuth(Arrays.asList("login", "plain"));
        connection.getSession().setAuthLoginCombined(true);

        ClientAuth auth = new ClientAuthMock();
        boolean process = auth.process(connection);

        assertFalse(process);

        connection.parseLines();
        assertEquals("AUTH LOGIN dG9ueUBleGFtcGxlLmNvbQ== Z2l2ZUhlclRoZVJpbmc=\r\n", connection.getLine(1));

        assertEquals(1, connection.getSession().getSessionTransactionList().getTransactions().size());
        assertEquals("535 5.7.1 Unauthorized", connection.getSession().getSessionTransactionList().getLast("AUTH").getResponse());
        assertTrue(connection.getSession().getSessionTransactionList().getLast("AUTH").isError());
    }


    @Test
    void processLoginCombinedRetry() throws IOException {
        StringBuilder stringBuilder = new StringBuilder()
                .append("555 5.5.2 Syntax error\r\n")
                .append("334 UGFzc3dvcmQ6\r\n")
                .append("235 2.7.0 Authorized\r\n");
        ConnectionMock connection = getConnection(stringBuilder);
        connection.getSession().setEhloAuth(Arrays.asList("login", "plain"));
        connection.getSession().setAuthLoginCombined(true);
        connection.getSession().setAuthLoginRetry(true);

        ClientAuth auth = new ClientAuthMock();
        boolean process = auth.process(connection);

        assertTrue(process);

        connection.parseLines();
        assertEquals("AUTH LOGIN dG9ueUBleGFtcGxlLmNvbQ== Z2l2ZUhlclRoZVJpbmc=\r\n", connection.getLine(1));

        assertEquals(1, connection.getSession().getSessionTransactionList().getTransactions().size());
        assertEquals("235 2.7.0 Authorized", connection.getSession().getSessionTransactionList().getLast("AUTH").getResponse());
        assertFalse(connection.getSession().getSessionTransactionList().getLast("AUTH").isError());
    }

    @Test
    void processPlain() throws IOException {
        StringBuilder stringBuilder = new StringBuilder()
                .append("235 2.7.0 Authorized\r\n");
        ConnectionMock connection = getConnection(stringBuilder);
        connection.getSession().setEhloAuth(Collections.singletonList("plain"));

        ClientAuth auth = new ClientAuthMock();
        boolean process = auth.process(connection);

        assertTrue(process);

        connection.parseLines();
        assertEquals("AUTH PLAIN dG9ueUBleGFtcGxlLmNvbQB0b255QGV4YW1wbGUuY29tAGdpdmVIZXJUaGVSaW5n\r\n", connection.getLine(1));

        assertEquals(1, connection.getSession().getSessionTransactionList().getTransactions().size());
        assertEquals("235 2.7.0 Authorized", connection.getSession().getSessionTransactionList().getLast("AUTH").getResponse());
        assertFalse(connection.getSession().getSessionTransactionList().getLast("AUTH").isError());
    }

    @Test
    void processPlainFalse() throws IOException {
        StringBuilder stringBuilder = new StringBuilder()
                .append("535 5.7.1 Unauthorized\r\n");
        ConnectionMock connection = getConnection(stringBuilder);
        connection.getSession().setEhloAuth(Collections.singletonList("plain"));

        ClientAuth auth = new ClientAuthMock();
        boolean process = auth.process(connection);

        assertFalse(process);

        connection.parseLines();
        assertEquals("AUTH PLAIN dG9ueUBleGFtcGxlLmNvbQB0b255QGV4YW1wbGUuY29tAGdpdmVIZXJUaGVSaW5n\r\n", connection.getLine(1));

        assertEquals(1, connection.getSession().getSessionTransactionList().getTransactions().size());
        assertEquals("535 5.7.1 Unauthorized", connection.getSession().getSessionTransactionList().getLast("AUTH").getResponse());
        assertTrue(connection.getSession().getSessionTransactionList().getLast("AUTH").isError());
    }
}

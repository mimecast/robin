package com.mimecast.robin.assertion;

import com.mimecast.robin.assertion.client.ExternalClient;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.main.Factories;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.transaction.EnvelopeTransactionList;
import com.mimecast.robin.smtp.transaction.Transaction;
import com.mimecast.robin.smtp.transaction.TransactionList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Assertion engine.
 *
 * <p>Called at the end of a client delivery.
 * <p>It will read the assertions from the configuration and try to assert them against SMTP transactions and external logs.
 * <p>External logs are only assertable given a client that can provide the logs.
 * <p>The intent of this was initially to fetch and assert MTA logs but was generified.
 *
 * @see Connection
 * @see ExternalClient
 */
public class Assert {

    /**
     * Session instance.
     */
    private final Connection connection;

    /**
     * Constructs a new Assert instance with given Connection.
     *
     * @param connection Connection instance.
     */
    public Assert(Connection connection) {
        this.connection = connection;
    }

    /**
     * Run assertions.
     *
     * @throws AssertException Assertion exception.
     */
    public void run() throws AssertException {
        if (!connection.getSession().getAssertions().isEmpty()) {
            assertSmtp(connection.getSession().getAssertions().getSmtp(), connection.getSessionTransactionList());
        }
        assertEnvelopes();
    }

    /**
     * Assert against transactions.
     *
     * @param list            SMTP assertions as a list in list.
     * @param transactionList TransactionList instance.
     * @throws AssertException Assertion exception.
     */
    private void assertSmtp(List<List<String>> list, TransactionList transactionList) throws AssertException {
        if (list != null && !list.isEmpty()) {
            for (List<String> assertion : list) {
                if (assertion.size() == 2) {
                    List<Transaction> transactions = transactionList.getTransactions(assertion.get(0));
                    if (transactions.isEmpty())
                        throw new AssertException("Assert unable to find transaction for [" + assertion.get(0) + "]");
                    assertTransactions(transactions, assertion.get(1));
                }
            }
        }
    }

    /**
     * Run regex assertions against transaction.
     *
     * @param transactions Transactions list.
     * @param regex        Assertion regex.
     * @throws AssertException Assertion exception.
     */
    private void assertTransactions(List<Transaction> transactions, String regex) throws AssertException {
        // No need to precompile patters as they are transaction specific.
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        boolean matched = false;
        for (Transaction transaction : transactions) {
            String response = transaction.getResponse();
            Matcher m = p.matcher(response);

            if (m.find()) {
                matched = true;
                break;
            }
        }

        if (!matched) throw new AssertException("Assert unable to match [" + regex + "]");
    }

    /**
     * Assert against session envelopes.
     *
     * @throws AssertException Assertion exception.
     */
    private void assertEnvelopes() throws AssertException {
        if (!connection.getSession().getEnvelopes().isEmpty() && !connection.getSessionTransactionList().getEnvelopes().isEmpty()) {
            for (int i = 0; i < connection.getSession().getEnvelopes().size(); i++) {
                MessageEnvelope envelope = connection.getSession().getEnvelopes().get(i);
                EnvelopeTransactionList envelopeTransactionList = connection.getSessionTransactionList().getEnvelopes().get(i);

                if (envelope.getAssertions() != null) {
                    if (!envelope.getAssertions().getSmtp().isEmpty()) {
                        assertSmtp(envelope.getAssertions().getSmtp(), envelopeTransactionList);
                    }

                    // External.
                    assertExternal(envelope, i);
                }
            }
        }
    }

    /**
     * Assert external logs if any.
     *
     * @param envelope MessageEnvelope instance.
     * @param transactionId Transaction ID.
     * @throws AssertException Assertion exception.
     */
    private void assertExternal(MessageEnvelope envelope, int transactionId) throws AssertException {
        if (!Factories.getExternalKeys().isEmpty()) {
            for (String key : Factories.getExternalKeys()) {

                BasicConfig basicConfig = envelope.getAssertions().getExternal(key);
                if (!basicConfig.isEmpty()) {

                    ExternalClient client = Factories.getExternalClient(key, connection, basicConfig, transactionId);
                    if (client != null) {
                        client.run();

                    } else {
                        throw new AssertException("Assert external client not instanciated");
                    }
                }
            }
        }
    }
}

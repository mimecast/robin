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
    protected final Connection connection;

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
        assertExternal(connection.getSession().getAssertions().getExternal());
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
            if (p.matcher(response).find()) {
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
                    assertExternal(envelope.getAssertions().getExternal(), i);
                }
            }
        }
    }

    /**
     * Assert external if any with given assertions list.
     *
     * @param assertions List of BasicConfig instances.
     * @throws AssertException Assertion exception.
     */
    protected void assertExternal(List<BasicConfig> assertions) throws AssertException {
        assertExternal(assertions, -1);
    }

    /**
     * Assert external if any with given assertions list and transaction id.
     *
     * @param assertions    List of BasicConfig instances.
     * @param transactionId Transaction ID.
     * @throws AssertException Assertion exception.
     */
    private void assertExternal(List<BasicConfig> assertions, int transactionId) throws AssertException {
        if (Factories.getExternalKeys().isEmpty()) return;

        List<String> keys = Factories.getExternalKeys();
        for (BasicConfig assertion : assertions) {
            if (!assertion.isEmpty() && keys.contains(assertion.getStringProperty("type"))) {

                ExternalClient client = Factories.getExternalClient(assertion.getStringProperty("type"), connection, assertion);
                if (client != null) {
                    if (transactionId >= 0) {
                        client.setTransactionId(transactionId);
                    }
                    client.run();

                } else {
                    throw new AssertException("Assert external client not instanciated");
                }
            }
        }
    }
}

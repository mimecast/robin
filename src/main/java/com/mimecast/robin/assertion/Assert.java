package com.mimecast.robin.assertion;

import com.mimecast.robin.assertion.client.ExternalClient;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Factories;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.transaction.EnvelopeTransactionList;
import com.mimecast.robin.smtp.transaction.Transaction;
import com.mimecast.robin.smtp.transaction.TransactionList;
import com.mimecast.robin.util.Magic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    protected static final Logger log = LogManager.getLogger(Assert.class);

    /**
     * Session instance.
     */
    protected final Connection connection;

    /**
     * Fail test for protocol assertion failure.
     */
    protected Boolean assertProtocolFails;

    /**
     * Skip asserting and exit gracefully.
     */
    protected Boolean skip = false;

    /**
     * Constructs a new Assert instance with given Connection.
     *
     * @param connection Connection instance.
     */
    public Assert(Connection connection) {
        this.connection = connection;
        this.assertProtocolFails = connection.getSession().getAssertions().getProtocolFails(Config.getClient().getAssertions().getProtocolFails());
    }

    /**
     * Have assertions been skipped?
     *
     * @return Boolean.
     */
    public boolean skipped() {
        return skip;
    }

    /**
     * Run assertions.
     *
     * @return Self.
     * @throws AssertException Assertion exception.
     */
    public Assert run() throws AssertException {
        if (!connection.getSession().getAssertions().isEmpty()) {
            assertProtocol(connection.getSession().getAssertions().getProtocol(), connection.getSession().getSessionTransactionList());
        }

        assertEnvelopes();
        assertExternal(connection.getSession().getAssertions().getExternal());

        return this;
    }

    /**
     * Assert against transactions.
     *
     * @param list            Protocol assertions as a list in list.
     * @param transactionList TransactionList instance.
     * @throws AssertException Assertion exception.
     */
    private void assertProtocol(List<List<String>> list, TransactionList transactionList) throws AssertException {
        if (list != null && !list.isEmpty()) {
            for (List<String> assertion : list) {
                if (assertion.size() == 2 && !skip) {
                    List<Transaction> transactions = transactionList.getTransactions(assertion.get(0));
                    if (transactions.isEmpty()) {
                        if (assertProtocolFails) {
                            throw new AssertException("Assert unable to find transaction for [" + assertion.get(0) + "]");
                        } else {
                            log.error("Assert unable to find transaction for [{}], skipping", assertion.get(0));
                            skip = true;
                            return;
                        }
                    }
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

        if (!matched) {
            if (assertProtocolFails) {
                throw new AssertException("Assert unable to match [" + regex + "]");
            } else {
                log.warn("Assert unable to match [{}], skipping", regex);
                skip = true;
            }
        }
    }

    /**
     * Assert against session envelopes.
     *
     * @throws AssertException Assertion exception.
     */
    private void assertEnvelopes() throws AssertException {
        if (skip) return; // Skip asserting and exit gracefully.

        if (!connection.getSession().getEnvelopes().isEmpty() && !connection.getSession().getSessionTransactionList().getEnvelopes().isEmpty()) {
            for (int i = 0; i < connection.getSession().getEnvelopes().size(); i++) {
                MessageEnvelope envelope = connection.getSession().getEnvelopes().get(i);
                List<EnvelopeTransactionList> envelopeTransactions = connection.getSession().getSessionTransactionList().getEnvelopes();
                if (envelopeTransactions.size() > i) {
                    EnvelopeTransactionList envelopeTransactionList = envelopeTransactions.get(i);

                    if (envelope.getAssertions() != null) {
                        if (!envelope.getAssertions().getProtocol().isEmpty()) {
                            assertProtocol(envelope.getAssertions().getProtocol(), envelopeTransactionList);
                        }

                        // External.
                        if (!envelope.getAssertions().getExternal().isEmpty()) {
                            // Add envelope and transaction magic for assertions to use.
                            Magic.putEnvelopeMagic(i, connection.getSession());
                            Magic.putTransactionMagic(i, connection.getSession());

                            assertExternal(envelope.getAssertions().getExternal(), i);
                        }
                    }
                } else {
                    throw new AssertException("Assert envelope " + i + " not found");
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
        if (Factories.getExternalKeys().isEmpty() || skip) return; // Skip asserting and exit gracefully.

        List<String> keys = Factories.getExternalKeys();
        for (BasicConfig assertion : assertions) {
            if (!assertion.isEmpty() && keys.contains(assertion.getStringProperty("type"))) {

                ExternalClient client = Factories.getExternalClient(assertion.getStringProperty("type"), connection, assertion);
                if (client != null) {
                    if (transactionId >= 0) {
                        client.setTransactionId(transactionId);
                    }
                    client.run();
                    skip = client.skip();

                } else {
                    throw new AssertException("Assert external client not instanciated");
                }
            }
        }
    }
}

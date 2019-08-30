package com.mimecast.robin.smtp.transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Session transaction list.
 * <p>This provides the implementation for session transactions.
 *
 * @see TransactionList
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class SessionTransactionList extends TransactionList {

    /**
     * Gets last SMTP transaction of defined verb.
     *
     * @param verb Verb string.
     * @return Transaction instance.
     */
    public Transaction getLast(String verb) {
        return !getTransactions(verb).isEmpty() ? getTransactions(verb).get((getTransactions(verb).size() - 1)) : null;
    }

    /**
     * Session envelopes.
     */
    private final List<EnvelopeTransactionList> envelopes = new ArrayList<>();

    /**
     * Adds envelope to list.
     *
     * @param envelopeTransactionList EnvelopeTransactionList instance.
     */
    public void addEnvelope(EnvelopeTransactionList envelopeTransactionList) {
        envelopes.add(envelopeTransactionList);
    }

    /**
     * Gets envelopes.
     *
     * @return List of EnvelopeTransactionList.
     */
    public List<EnvelopeTransactionList> getEnvelopes() {
        return envelopes;
    }
}

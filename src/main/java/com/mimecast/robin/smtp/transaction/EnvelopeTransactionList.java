package com.mimecast.robin.smtp.transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * EnvelopeTransactionList
 * <p>This provides the implementation for envelope transactions.
 *
 * @see TransactionList
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class EnvelopeTransactionList extends TransactionList {

    /**
     * Gets MAIL transaction.
     *
     * @return MAIL transaction instance.
     */
    public Transaction getMail() {
        return !getTransactions("MAIL").isEmpty() ? getTransactions("MAIL").get(0) : null;
    }

    /**
     * Gets RCPT transactions.
     *
     * @return RCPT transactions list.
     */
    public List<Transaction> getRcpt() {
        return getTransactions("RCPT");
    }

    /**
     * Gets RCPT errors logs.
     *
     * @return List of Transaction.
     */
    public List<Transaction> getRcptErrors() {
        List<Transaction> found = new ArrayList<>();
        for (Transaction transaction : getRcpt()) {
            if (transaction.isError()) {
                found.add(transaction);
            }
        }

        return found;
    }

    /**
     * Gets DATA transaction.
     *
     * @return DATA transaction instance.
     */
    public Transaction getData() {
        return !getTransactions("DATA").isEmpty() ? getTransactions("DATA").get(0) : null;
    }

    /**
     * Gets BDAT transactions.
     *
     * @return BDAT transactions list.
     */
    public List<Transaction> getBdat() {
        return getTransactions("BDAT");
    }
}

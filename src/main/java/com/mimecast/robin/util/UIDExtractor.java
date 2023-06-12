package com.mimecast.robin.util;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UID Extractor.
 */
public class UIDExtractor {

    /**
     * UID pattern.
     */
    protected static final Pattern uidPattern;
    static {
        uidPattern = Pattern.compile(
                Config.getProperties().getStringProperty("uidPattern", Config.getProperties().getStringProperty("uid.pattern", "\\s\\[([a-z0-9\\-_]+)]")),
                Pattern.CASE_INSENSITIVE
        );
    }

    /**
     * Protected constructor.
     */
    private UIDExtractor() {
        throw new IllegalStateException("Static class");
    }

    /**
     * Get UID from SMTP command response with given conenction and transaction ID.
     *
     * @param connection    Connection instance.
     * @param transactionId Transaction ID.
     * @return String.
     */
    public static String getUID(Connection connection, int transactionId) {
        List<Transaction> transactions = new ArrayList<>();

        // Select transactions.
        if (transactionId >= 0) {
            transactions.addAll(connection.getSessionTransactionList().getEnvelopes().get(transactionId).getTransactions());
        } else {
            transactions.addAll(connection.getSessionTransactionList().getTransactions());
        }

        // Match UID pattern to transaction response.
        for (Transaction transaction : transactions) {
            if (transaction.getResponse() != null && !transaction.getResponse().isEmpty()) {
                Matcher m = uidPattern.matcher(transaction.getResponse());
                if (m.find()) {
                    return m.group(1).replaceAll("^-+", "");
                }
            }
        }

        return null;
    }
}

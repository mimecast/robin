package com.mimecast.robin.assertion.mta;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.assertion.mta.client.LogsClient;
import com.mimecast.robin.config.ConfigFoundation;
import com.mimecast.robin.config.assertion.AssertMtaConfig;
import com.mimecast.robin.main.Config;
import com.mimecast.robin.smtp.transaction.EnvelopeTransactionList;
import com.mimecast.robin.smtp.transaction.Transaction;
import com.mimecast.robin.util.Sleep;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Assert MTA logs.
 * <p>This runs the asserts configured against the MTA log lines to find matches.
 * <p>The server logs will be pulled using the given LogsClient instance.
 *
 * <p>It was designed having in mind the type of logs Log4j produces.
 * <p>As such these logs should be searchable by a unique ID (UID for short).
 * <p>At Mimecast this is provided in single line SMTP responses except the first.
 * <p>I chose to use the MAIL response as its first in the envelope.
 * <p>Different MTAs behave differently but everyone's got at least one.
 *
 * <p>Gmail for example:
 * <pre>
 *       $ telnet gmail-smtp-in.l.google.com 25
 *       Trying 64.233.184.27...
 *       Connected to gmail-smtp-in.l.google.com.
 *       Escape character is '^]'.
 *       220 mx.google.com ESMTP o13si19240726wrj.243 - gsmtp
 *       QUIT
 *       221 2.0.0 closing connection o13si19240726wrj.243 - gsmtp
 * </pre>
 *
 * @see LogsClient
 * @see AssertGroup
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class AssertMta {
    private static final Logger log = LogManager.getLogger(AssertMta.class);

    /**
     * Assertion config.
     */
    private final AssertMtaConfig assertions;

    /**
     * EnvelopeTransactionList instance.
     */
    private final EnvelopeTransactionList envelopeTransactionList;

    /**
     * Logs client instance.
     */
    private final LogsClient client;

    /**
     * UID.
     */
    private String uid;

    /**
     * Logs list.
     */
    private List<Object> logsList;

    /**
     * Verify patterns.
     */
    private final List<Pattern> verifyPatterns = new ArrayList<>();

    /**
     * Compiled patterns.
     */
    private List<AssertGroup> patternGroups;

    /**
     * Constructs a new AssertMta instance.
     *
     * @param client                  LogsClient instance.
     * @param assertions              AssertMtaConfig instance.
     * @param envelopeTransactionList EnvelopeTransactionList instance.
     * @throws AssertException Assertion exception.
     */
    public AssertMta(LogsClient client, AssertMtaConfig assertions, EnvelopeTransactionList envelopeTransactionList) throws AssertException {
        this.assertions = assertions;
        this.envelopeTransactionList = envelopeTransactionList;
        this.client = client;

        try {
            findUID(); // No point in doing anything if we can't get it.
            compileVerify(); // Precompile verify patterns for performance.
            findLogs(); // Get the logs for that UID and verify.
            compilePatterns(); // Precompile match patterns for performance.
            matchPatterns(); // Match patters to log lines.
            verifyMatches(); // Evaluate unmatched assertion and except.
        } catch (Exception e) {
            throw new AssertException(e);
        }
    }

    /**
     * Get UID from SMTP command response.
     *
     * @throws AssertException Assertion exception.
     */
    @SuppressWarnings("unchecked")
    private void findUID() throws AssertException {
        // Gets MTA UID from assertions config or defaults to properties.
        Map<String, String> map;
        if (assertions.hasProperty("uid")) {
            map = assertions.getMapProperty("uid");
        } else {
            map = Config.getProperties().getMapProperty("mta.uid");
        }
        MtaUid mtaUid = new MtaUid(map);

        // Select transaction for configured command.
        Transaction transaction = null;
        List<Transaction> transactions;
        switch (mtaUid.getCommand().toUpperCase()) {
            case "RCPT":
                transactions = envelopeTransactionList.getRcpt();
                if (!transactions.isEmpty()) {
                    transaction = transactions.get(0);
                }
                break;
            case "DATA":
                transaction = envelopeTransactionList.getData();
                break;
            case "BDAT":
                transactions = envelopeTransactionList.getBdat();
                if (!transactions.isEmpty()) {
                    transaction = transactions.get(transactions.size() - 1);
                }
                break;
            default:
                transaction = envelopeTransactionList.getMail();
                break;
        }

        // Match UID pattern to transaction response.
        if (transaction != null && transaction.getResponse().startsWith("250 ")) {
            Matcher m = mtaUid.getPattern().matcher(transaction.getResponse());
            if (m.find()) {
                this.uid = m.group(1).replaceAll("^-+", "");
                return;
            }
        }

        throw new AssertException("Cannot find UID");
    }

    /**
     * Find logs by UID using given client if any.
     */
    private void findLogs() {
        long delay = assertions.getWait() > 0 ? assertions.getWait() : 2L; // Initial wait 2 seconds.
        for (int count = 0; count < assertions.getRetry(); count++) {
            Sleep.nap((int) delay);

            JSONArray logs = client.getLogs(uid);
            if (logs != null && !logs.isEmpty()) {
                logsList = logs.toList();
                if (verifyLogs()) {
                    break;
                }
            }

            delay = assertions.getDelay(); // Retry delay.
        }
    }

    /**
     * Precompile verify patterns for performance.
     */
    private void compileVerify() {
        for (String assertion : assertions.getVerify()) {
            verifyPatterns.add(Pattern.compile(assertion, Pattern.CASE_INSENSITIVE));
        }
    }

    /**
     * Check we got complete logs.
     */
    private boolean verifyLogs() {
        if (logsList != null) {

            // Loop log lines.
            for (Object line : logsList) {
                if (line instanceof String && verifyLog((String) line)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check log line.
     *
     * @param line Log line.
     * @return Boolean.
     */
    private boolean verifyLog(String line) {
        boolean valid = true;

        for (Pattern pattern : verifyPatterns) {
            Matcher m = pattern.matcher(line);
            if (!m.find()) {
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Precompile match patterns for performance.
     */
    private void compilePatterns() {
        this.patternGroups = new ArrayList<>();

        // Make new list of assertions with precompiled patterns for performance.
        // Additionally we need a result field to track matches.
        for (List<String> list : assertions.getMatch()) {
            List<Pattern> compiled = new ArrayList<>();
            for (String assertion : list) {
                compiled.add(Pattern.compile(assertion, Pattern.CASE_INSENSITIVE));
            }

            patternGroups.add(new AssertGroup().setRules(list).setPatterns(compiled));
        }
    }

    /**
     * Match log lines to patterns.
     */
    private void matchPatterns() {
        if (logsList != null) {

            // Loop log lines.
            for (Object line : logsList) {
                if (line instanceof String) {
                    matchLine((String) line);
                }
            }
        }
    }

    /**
     * Match log line to patterns.
     *
     * @param line Log line.
     */
    private void matchLine(String line) {
        for (AssertGroup group : patternGroups) {

            // Skip matched.
            if (!group.hasMatched()) {

                // Loop and match.
                for (Pattern pattern : group.getPatterns()) {
                    Matcher m = pattern.matcher(line);
                    if (m.find()) {
                        group.addMatched(pattern);
                    }
                }

                // Evaluate if all patterns matched.
                if (group.getMatched().size() == group.getPatterns().size()) {
                    log.debug("LOG: {}", line);
                    log.debug("MATCH: {}", group.getMatched().toString());
                }
            }
        }
    }

    /**
     * Check all matches matched.
     */
    private void verifyMatches() throws AssertException {
        for (AssertGroup group : patternGroups) {
            if (!group.hasMatched()) {
                throw new AssertException("Unable to find pattern " + group.getUnmatched() + " in logs");
            }
        }
    }

    /**
     * MTA UID config handler.
     */
    private static class MtaUid extends ConfigFoundation {

        /**
         * UID command.
         */
        private final String command;

        /**
         * UID regex.
         */
        private final String regex;

        /**
         * Compiled regex.
         */
        private final Pattern pattern;

        /**
         * Constructs a new MtaUid instance with given map.
         *
         * @param map Map.
         */
        MtaUid(Map<String, String> map) throws AssertException {
            super(map);
            command = getStringProperty("command");
            regex = getStringProperty("pattern");
            pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

            if (StringUtils.isBlank(command) || StringUtils.isBlank(regex)) {
                throw new AssertException("Cannot find UID pattern");
            }
        }

        /**
         * Gets UID command.
         */
        String getCommand() {
            return command;
        }

        /**
         * Gets UID regex.
         */
        Pattern getPattern() {
            return pattern;
        }

        /**
         * To string.
         *
         * @return Regex string.
         */
        public String toString() {
            return regex;
        }
    }
}

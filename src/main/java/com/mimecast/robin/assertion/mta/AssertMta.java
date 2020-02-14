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
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 * @see LogsClient
 * @see AssertGroup
 */
public class AssertMta {
    private static final Logger log = LogManager.getLogger(AssertMta.class);

    /**
     * Assertion config.
     */
    private final AssertMtaConfig assertMtaConfig;

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
    private final List<AssertGroup> matchGroups = new ArrayList<>();
    private final List<AssertGroup> refuseGroups = new ArrayList<>();

    /**
     * Constructs a new AssertMta instance.
     *
     * @param client                  LogsClient instance.
     * @param assertMtaConfig         AssertMtaConfig instance.
     * @param envelopeTransactionList EnvelopeTransactionList instance.
     * @throws AssertException Assertion exception.
     */
    public AssertMta(LogsClient client, AssertMtaConfig assertMtaConfig, EnvelopeTransactionList envelopeTransactionList) throws AssertException {
        this.assertMtaConfig = assertMtaConfig;
        log.debug("Assert MTA Config: {}", assertMtaConfig.getMap());

        this.envelopeTransactionList = envelopeTransactionList;
        this.client = client;

        try {
            findUID(); // No point in doing anything if we can't get it.
            compileVerify(); // Precompile verify patterns for performance.
            findLogs(); // Get the logs for that UID and verify.
            compilePatterns(assertMtaConfig.getMatch(), matchGroups); // Precompile match patterns for performance.
            compilePatterns(assertMtaConfig.getRefuse(), refuseGroups); // Precompile refuse patterns for performance.
            checkPatterns(true); // Match patters to log lines.
            checkPatterns(false); // Refuse patters to log lines.
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
        if (assertMtaConfig.hasProperty("uid")) {
            map = assertMtaConfig.getMapProperty("uid");
        } else {
            map = Config.getProperties().getMapProperty("mta.uid");
        }
        MtaUid mtaUid = new MtaUid(map);
        log.debug("Assert MTA UID: {}", mtaUid.getMap());

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
    private void findLogs() throws AssertException {
        long delay = assertMtaConfig.getWait() > 0 ? assertMtaConfig.getWait() * 1000L : 2000L; // Initial wait 2 seconds.
        for (int count = 0; count < assertMtaConfig.getRetry(); count++) {
            Sleep.nap((int) delay);
            log.info("Assert MTA logs fetch attempt {} of {}", count + 1, assertMtaConfig.getRetry());

            JSONArray logs = client.getLogs(uid);
            if (logs != null && !logs.isEmpty()) {
                logsList = logs.toList();
                if (verifyLogs()) {
                    log.debug("Assert MTA logs fetch verify success");
                    break;
                }
            } else {
                log.debug("Assert MTA logs fetch got none");
            }

            delay = assertMtaConfig.getDelay(); // Retry delay.
            log.info("Assert MTA logs fetch verify {}", (count < assertMtaConfig.getRetry() - 1 ? "failure" : "attempts spent"));
        }

        if (logsList == null || logsList.isEmpty()) {
            throw new AssertException("No logs found to assert against");
        }
    }

    /**
     * Precompile verify patterns for performance.
     */
    private void compileVerify() {
        for (String assertion : assertMtaConfig.getVerify()) {
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
     * Precompile match and refuse patterns for performance.
     */
    private void compilePatterns(List<List<String>> groups, List<AssertGroup> container) {
        // Make new list of assertions with precompiled patterns for performance.
        // Additionally we need a result field to track matches.
        for (List<String> list : groups) {
            List<Pattern> compiled = new ArrayList<>();
            for (String assertion : list) {
                compiled.add(Pattern.compile(assertion, Pattern.CASE_INSENSITIVE));
            }

            container.add(new AssertGroup().setRules(list).setPatterns(compiled));
        }
    }

    /**
     * Match log lines to patterns.
     *
     * @param positive Success on match.
     */
    private void checkPatterns(boolean positive) throws AssertException {
        if (logsList != null) {

            // Loop log lines.
            for (Object line : logsList) {
                if (line instanceof String) {
                    checkLine((String) line, positive);
                }
            }
        }
    }

    /**
     * Match log line to patterns.
     *
     * @param line     Log line.
     * @param positive Success on match.
     */
    private void checkLine(String line, boolean positive) throws AssertException {
        // Choose positive or negative matching groups
        List<AssertGroup> groups = positive ? matchGroups : refuseGroups;

        for (AssertGroup group : groups) {

            // Skip matched.
            if (!group.hasMatched()) {

                // Loop and match.
                matchLine(group, line, positive);

                // Evaluate if all positive patterns matched.
                if (positive && group.getMatched().size() == group.getPatterns().size()) {
                    log.debug("Assert MTA LOG: {}", line);
                    log.info("Assert MTA matched: {}", group.getMatched().toString());
                }
            }
        }
    }

    /**
     * Match log line to pattern group.
     *
     * @param group    AssertGroup instance.
     * @param line     Log line.
     * @param positive Success on match.
     */
    private void matchLine(AssertGroup group, String line, boolean positive) throws AssertException {
        for (Pattern pattern : group.getPatterns()) {
            Matcher m = pattern.matcher(line);
            if (m.find()) {
                group.addMatched(pattern);

                // Break on first negative match
                if (!positive) {
                    log.debug("Assert MTA log: {}", line);
                    log.error("Assert MTA not matched: {}", group.getMatched().toString());
                    throw new AssertException("Found refuse pattern " + group.getMatched() + " in logs");
                }
            }
        }
    }

    /**
     * Check all matches matched.
     */
    private void verifyMatches() throws AssertException {
        for (AssertGroup group : matchGroups) {
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

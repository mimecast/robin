package com.mimecast.robin.assertion;

import com.mimecast.robin.assertion.client.ExternalClient;
import com.mimecast.robin.config.assertion.AssertExternalConfig;
import com.mimecast.robin.util.Sleep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Assert external logs.
 *
 * <p>Asserts against external service log lines.
 * <p>Logs can be pulled using the given ExternalClient instance.
 *
 * @see ExternalClient
 * @see AssertExternalGroup
 */
public class AssertExternal {
    private static final Logger log = LogManager.getLogger(AssertExternal.class);

    /**
     * ExternalClient instance.
     */
    private final ExternalClient client;

    /**
     * AssertExternalConfig instance.
     */
    private final AssertExternalConfig assertExternalConfig;

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
    private final List<AssertExternalGroup> matchGroups = new ArrayList<>();
    private final List<AssertExternalGroup> refuseGroups = new ArrayList<>();

    /**
     * Constructs a new AssertExternal instance.
     *
     * @param client               ExternalClient instance.
     * @param assertExternalConfig AssertExternalConfig instance.
     * @throws AssertException Assertion exception.
     */
    public AssertExternal(ExternalClient client, AssertExternalConfig assertExternalConfig) throws AssertException {
        this.assertExternalConfig = assertExternalConfig;
        log.debug("AssertExternal Config: {}", assertExternalConfig.getMap());

        this.client = client;

        if (!assertExternalConfig.isEmpty()) {
            try {
                compileVerify(); // Precompile verify patterns for performance.
                findLogs(); // Get the logs for that UID and verify.
                compilePatterns(assertExternalConfig.getMatch(), matchGroups); // Precompile match patterns for performance.
                compilePatterns(assertExternalConfig.getRefuse(), refuseGroups); // Precompile refuse patterns for performance.
                checkPatterns(true); // Match patters to log lines.
                checkPatterns(false); // Refuse patters to log lines.
                verifyMatches(); // Evaluate unmatched assertion and except.
            } catch (Exception e) {
                throw new AssertException(e);
            }
        }
    }

    /**
     * Find logs using given client if any.
     *
     * @throws AssertException Assertion exception.
     */
    private void findLogs() throws AssertException {
        long delay = assertExternalConfig.getWait() > 0 ? assertExternalConfig.getWait() * 1000L : 2000L; // Initial wait 2 seconds.
        for (int count = 0; count < assertExternalConfig.getRetry(); count++) {
            Sleep.nap((int) delay);
            log.info("AssertExternal logs fetch attempt {} of {}", count + 1, assertExternalConfig.getRetry());

            JSONArray logs = client.getLogs();
            if (logs != null && !logs.isEmpty()) {
                logsList = logs.toList();
                if (verifyLogs()) {
                    log.debug("AssertExternal logs fetch verify success");
                    break;
                }
            } else {
                log.debug("AssertExternal logs fetch got none");
            }

            delay = assertExternalConfig.getDelay() * 1000L; // Retry delay.
            log.info("AssertExternal logs fetch verify {}", (count < assertExternalConfig.getRetry() - 1 ? "failure" : "attempts spent"));
        }

        if (logsList == null || logsList.isEmpty()) {
            throw new AssertException("No logs found to assert against");
        }
    }

    /**
     * Precompile verify patterns for performance.
     */
    private void compileVerify() {
        for (String assertion : assertExternalConfig.getVerify()) {
            verifyPatterns.add(Pattern.compile(assertion, Pattern.CASE_INSENSITIVE));
        }
    }

    /**
     * Check we got complete logs.
     *
     * @return Boolean.
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
     *
     * @param groups    List of List of String.
     * @param container List of AssertGroup instances.
     */
    private void compilePatterns(List<List<String>> groups, List<AssertExternalGroup> container) {
        // Make new list of assertions with precompiled patterns for performance.
        // Additionally we need a result field to track matches.
        for (List<String> list : groups) {
            List<Pattern> compiled = new ArrayList<>();
            for (String assertion : list) {
                compiled.add(Pattern.compile(assertion, Pattern.CASE_INSENSITIVE));
            }

            container.add(new AssertExternalGroup().setPatterns(compiled));
        }
    }

    /**
     * Match log lines to patterns.
     *
     * @param positive Success on match.
     * @throws AssertException Assertion exception.
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
     * @throws AssertException Assertion exception.
     */
    private void checkLine(String line, boolean positive) throws AssertException {
        // Choose positive or negative matching groups
        List<AssertExternalGroup> groups = positive ? matchGroups : refuseGroups;

        for (AssertExternalGroup group : groups) {

            // Skip matched.
            if (!group.hasMatched()) {

                // Loop and match.
                matchLine(group, line, positive);

                // Evaluate if all positive patterns matched.
                if (positive && group.getMatched().size() == group.getPatterns().size()) {
                    log.debug("AssertExternal LOG: {}", line);
                    log.info("AssertExternal matched: {}", group.getMatched().toString());
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
     * @throws AssertException Assertion exception.
     */
    private void matchLine(AssertExternalGroup group, String line, boolean positive) throws AssertException {
        for (Pattern pattern : group.getPatterns()) {
            Matcher m = pattern.matcher(line);
            if (m.find()) {
                group.addMatched(pattern);

                // Break on first negative match
                if (!positive) {
                    log.debug("AssertExternal log: {}", line);
                    log.error("AssertExternal not matched: {}", group.getMatched().toString());
                    throw new AssertException("Found refuse pattern " + group.getMatched() + " in logs");
                }
            }
        }
    }

    /**
     * Check all matches matched.
     *
     * @throws AssertException Assertion exception.
     */
    private void verifyMatches() throws AssertException {
        for (AssertExternalGroup group : matchGroups) {
            if (!group.hasMatched()) {
                throw new AssertException("Unable to find pattern " + group.getUnmatched() + " in logs");
            }
        }
    }
}

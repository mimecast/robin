package com.mimecast.robin.assertion.client;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.assertion.AssertExternalGroup;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.config.assertion.external.MatchExternalClientConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Match external client.
 * <p>This provides a means to match regular expressions against strings.
 */
public abstract class MatchExternalClient extends ExternalClient {

    /**
     * Assert external config instance.
     */
    protected MatchExternalClientConfig config;

    /**
     * Verify no data found.
     */
    protected Boolean verifyNone = false;

    /**
     * Verify patterns.
     */
    protected final List<Pattern> verifyPatterns = new ArrayList<>();

    /**
     * Compiled patterns.
     */
    protected final List<AssertExternalGroup> matchGroups = new ArrayList<>();
    protected final List<AssertExternalGroup> refuseGroups = new ArrayList<>();

    /**
     * Sets config instance.
     *
     * @param config MatchExternalClient instance.
     * @return Self.
     */
    @Override
    public MatchExternalClient setConfig(BasicConfig config) {
        this.config = new MatchExternalClientConfig(config.getMap());
        return this;
    }

    /**
     * Run matches.
     *
     * @param list List of String.
     * @throws AssertException Assertion exception.
     */
    public void runMatches(List<String> list) throws AssertException {
        compilePatterns(config.getMatch(connection, transactionId), matchGroups); // Precompile match patterns for performance.
        compilePatterns(config.getRefuse(), refuseGroups); // Precompile refuse patterns for performance.

        checkPatterns(list, true); // Match patters to log lines.
        checkPatterns(list, false); // Refuse patters to log lines.

        verifyMatches(); // Evaluate unmatched assertion and except.
        magicMatches(list); // Evaluate magic matches and record findings in Session magic.
    }

    /**
     * Precompile verify patterns for performance.
     */
    protected void compileVerify() {
        for (String assertion : config.getVerify()) {
            verifyPatterns.add(Pattern.compile(assertion, Pattern.CASE_INSENSITIVE));
        }
    }

    /**
     * Precompile match and refuse patterns for performance.
     *
     * @param groups    List of List of String.
     * @param container List of AssertGroup instances.
     */
    protected void compilePatterns(List<List<String>> groups, List<AssertExternalGroup> container) {
        // Make new list of assertions with precompiled patterns for performance.
        // Additionally, we need a result field to track matches.
        for (List<String> list : groups) {
            List<Pattern> compiled = new ArrayList<>();
            for (String assertion : list) {
                compiled.add(Pattern.compile(assertion, Pattern.CASE_INSENSITIVE));
            }

            container.add(new AssertExternalGroup().setPatterns(compiled));
        }
    }

    /**
     * Check data is complete.
     *
     * @param list List of String.
     * @return Boolean.
     */
    protected boolean checkVerify(List<String> list) {
        if (list != null) {

            // Loop log lines.
            for (String entry : list) {
                if (checkVerifyEntry(entry)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check data entry.
     *
     * @param entry String.
     * @return Boolean.
     */
    protected boolean checkVerifyEntry(String entry) {
        boolean valid = true;

        for (Pattern pattern : verifyPatterns) {
            if (!pattern.matcher(entry).find()) {
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Match data entries to patterns.
     *
     * @param list     List of String.
     * @param positive Success on match.
     * @throws AssertException Assertion exception.
     */
    protected void checkPatterns(List<String> list, boolean positive) throws AssertException {
        if (list != null) {
            for (String line : list) {
                matchPatterns(line, positive);
            }

            // Evaluate if all positive patterns matched.
            if (positive) {
                for (AssertExternalGroup group : matchGroups) {
                    if (group.getMatched().size() == group.getPatterns().size()) {
                        log.info("AssertExternal matched: {}", group.getMatched());
                    }
                }
            }
        }
    }

    /**
     * Match data entry to patterns.
     *
     * @param entry    Data entry.
     * @param positive Success on match.
     * @throws AssertException Assertion exception.
     */
    protected void matchPatterns(String entry, boolean positive) throws AssertException {
        // Choose positive or negative matching groups
        List<AssertExternalGroup> groups = positive ? matchGroups : refuseGroups;

        for (AssertExternalGroup group : groups) {
            // Skip matched.
            if (!group.hasMatched()) {

                // Loop and match.
                matchEntry(group, entry, positive);
            }
        }
    }

    /**
     * Match data entry to pattern group.
     *
     * @param group    AssertGroup instance.
     * @param entry    Log entry.
     * @param positive Success on match.
     * @throws AssertException Assertion exception.
     */
    protected void matchEntry(AssertExternalGroup group, String entry, boolean positive) throws AssertException {
        for (Pattern pattern : group.getPatterns()) {
            Matcher m = pattern.matcher(entry);
            if (m.find()) {
                group.addMatched(pattern);


                // Break on first negative match
                if (!positive) {
                    log.debug("AssertExternal log: {}", entry);
                    log.error("AssertExternal matched refuse: {}", group.getMatched());
                    if (!assertVerifyFails) {
                        skip = true;
                        log.warn("Skipping");
                        return;
                    }
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
    protected void verifyMatches() throws AssertException {
        for (AssertExternalGroup group : matchGroups) {
            if (!group.hasMatched()) {
                throw new AssertException("Unable to find pattern " + group.getUnmatched() + " in data");
            }
        }
    }

    /**
     * Record matches in Session magic.
     * <p>Record group 1 is there is one else full match.
     *
     * @param list List of String.
     */
    protected void magicMatches(List<String> list) {
        if (list != null) {
            Map<String, Pattern> patterns = new HashMap<>();
            for (Map<String, String> magic : config.getMagic()) {
                patterns.put(magic.get("name"), Pattern.compile(magic.get("pattern"), Pattern.CASE_INSENSITIVE));
            }

            for (Object line : list) {
                if (line instanceof String) {
                    for (Map.Entry<String, Pattern> pattern : patterns.entrySet()) {
                        Matcher m = pattern.getValue().matcher((String) line);
                        if (m.find()) {
                            String group = m.groupCount() == 0 ? m.group() : m.group(1);
                            connection.getSession().putMagic(pattern.getKey(), group);
                            log.info("AssertExternal matched and saved magic: {} = {}", pattern.getKey(), group);
                        }
                    }
                }
            }
        }
    }

    /**
     * Log results.
     *
     * @param list List of String.
     */
    @SuppressWarnings("rawtypes")
    public void logResults(List<String> list) {
        if (list.isEmpty()) {
            log.trace("Results: {}", list);
        } else {
            log.trace("Results:");
            for (Object line : list) {
                log.trace(line);
            }
        }
    }
}

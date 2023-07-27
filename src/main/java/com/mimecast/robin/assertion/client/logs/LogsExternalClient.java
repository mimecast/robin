package com.mimecast.robin.assertion.client.logs;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.assertion.AssertExternalGroup;
import com.mimecast.robin.assertion.client.ExternalClient;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.config.assertion.external.logs.LogsExternalClientConfig;
import com.mimecast.robin.main.Config;
import com.mimecast.robin.util.Sleep;
import com.mimecast.robin.util.UIDExtractor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Logs external client.
 * <p>This provides a means to fetch the logs from a local file.
 * TODO Extend this to implement remote log pulling via SSH.
 */
public class LogsExternalClient extends ExternalClient {

    /**
     * Assert external config instance.
     */
    protected LogsExternalClientConfig config;

    /**
     * Logs list.
     */
    protected List<Object> logsList;

    /**
     * Verify no logs found.
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

    // Storage dir and log file.
    protected String dir;
    protected String fileName;
    protected String path;

    /**
     * Constructs a new LogsExternalClient instance.
     */
    public LogsExternalClient() {
        dir = Config.getProperties().getStringProperty("localLogsDir", Config.getProperties().getStringProperty("logs.local.dir", ""));
        fileName = new SimpleDateFormat("yyyyMMdd", Config.getProperties().getLocale()).format(new Date()) + ".log";

        setPath(fileName);
    }

    /**
     * Sets config instance.
     *
     * @param config ExternalClient instance.
     * @return Self.
     */
    @Override
    public ExternalClient setConfig(BasicConfig config) {
        this.config = new LogsExternalClientConfig(config.getMap());

        // Update file path.
        setPath(config.getStringProperty("logPrecedence", "") + fileName);
        verifyNone = config.getBooleanProperty("verifyNone", false);

        return this;
    }

    /**
     * Sets path.
     *
     * @param fileName String.
     */
    protected void setPath(String fileName) {
        path = Paths.get(dir, fileName).toString();
    }

    /**
     * Run assertions.
     */
    @Override
    public void run() throws AssertException {
        if (!config.isEmpty()) {
            try {
                if (!verifyNone) {
                    compileVerify(); // Precompile verify patterns for performance.
                }
                findLogs(); // Get the logs for that UID and verify.

                // Verify no logs were found.
                if (verifyNone) {
                    log.info("AssertExternal logs verify none");

                } else {
                    compilePatterns(config.getMatch(connection, transactionId), matchGroups); // Precompile match patterns for performance.
                    compilePatterns(config.getRefuse(), refuseGroups); // Precompile refuse patterns for performance.
                    checkPatterns(true); // Match patters to log lines.
                    checkPatterns(false); // Refuse patters to log lines.
                    verifyMatches(); // Evaluate unmatched assertion and except.
                    magicMatches(); // Evaluate magic matches and record findings in Session magic.
                }
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
    protected void findLogs() throws AssertException {
        logsList = new ArrayList<>();

        if (dir.isEmpty()) {
            log.error("AssertExternal logs.local.dir not found in properties");

        } else {
            long delay = config.getWait() > 0 ? config.getWait() * 1000L : 0L;
            for (int count = 0; count < config.getRetry(); count++) {
                Sleep.nap((int) delay);
                log.info("AssertExternal logs fetching locally");

                try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                    String uid = UIDExtractor.getUID(connection, transactionId);
                    List<Pattern> patterns = getGreps();

                    String line;
                    while ((line = br.readLine()) != null) {
                        String finalLine = line;
                        if ((patterns.isEmpty() && (uid == null || line.contains(uid))) || patterns.stream().allMatch(pattern -> pattern.matcher(finalLine).find())) {
                            logsList.add(line);
                        }
                    }

                    logResults(logsList);

                } catch (IOException e) {
                    log.error("AssertExternal logs reading problems: {}", e.getMessage());
                    throw new AssertException("No logs found to assert against");
                }

                if (verifyNone) break;

                if (verifyLogs()) {
                    log.debug("AssertExternal logs fetch verify success");
                    break;
                }

                delay = config.getDelay() * 1000L; // Retry delay.
                log.info("AssertExternal logs fetch verify {}", (count < config.getRetry() - 1 ? "failure" : "attempts spent"));

                if (!assertVerifyFails) {
                    skip = true;
                    log.warn("Skipping");
                    return;
                }
            }

            if (logsList == null || logsList.isEmpty()) {
                if (!verifyNone) {
                    throw new AssertException("No logs found to assert against");
                }
            }
        }
    }

    /**
     * Prepare grep patterns.
     */
    @SuppressWarnings("unchecked")
    protected List<Pattern> getGreps() {
        List<String> greps = ((List<Map<String, String>>) config.getListProperty("grep")).stream()
                .filter(map -> !map.containsKey("parameter") || !map.get("parameter").contains("v")) // `grep -v` skip patterns.
                .map(map -> {
                    String pattern = connection.getSession().transactionMagicReplace(map.get("pattern"), connection, transactionId);
                    return map.containsKey("parameter") && map.get("parameter").contains("E") ? pattern : Pattern.quote(pattern); // `grep -E` compile as is or escape.
                })
                .collect(Collectors.toList());

        return greps.stream()
                .map(string -> Pattern.compile(string, Pattern.CASE_INSENSITIVE))
                .collect(Collectors.toList());
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
     * Check we got complete logs.
     *
     * @return Boolean.
     */
    protected boolean verifyLogs() {
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
    protected boolean verifyLog(String line) {
        boolean valid = true;

        for (Pattern pattern : verifyPatterns) {
            if (!pattern.matcher(line).find()) {
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
     * Match log lines to patterns.
     *
     * @param positive Success on match.
     * @throws AssertException Assertion exception.
     */
    protected void checkPatterns(boolean positive) throws AssertException {
        if (logsList != null) {
            for (Object line : logsList) {
                if (line instanceof String) {
                    checkLine((String) line, positive);
                }
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
     * Match log line to patterns.
     *
     * @param line     Log line.
     * @param positive Success on match.
     * @throws AssertException Assertion exception.
     */
    protected void checkLine(String line, boolean positive) throws AssertException {
        // Choose positive or negative matching groups
        List<AssertExternalGroup> groups = positive ? matchGroups : refuseGroups;

        for (AssertExternalGroup group : groups) {
            // Skip matched.
            if (!group.hasMatched()) {

                // Loop and match.
                matchLine(group, line, positive);
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
    protected void matchLine(AssertExternalGroup group, String line, boolean positive) throws AssertException {
        for (Pattern pattern : group.getPatterns()) {
            Matcher m = pattern.matcher(line);
            if (m.find()) {
                group.addMatched(pattern);


                // Break on first negative match
                if (!positive) {
                    log.debug("AssertExternal log: {}", line);
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
                throw new AssertException("Unable to find pattern " + group.getUnmatched() + " in logs");
            }
        }
    }

    /**
     * Record matches in Session magic.
     * <p>Record group 1 is there is one else full match.
     */
    protected void magicMatches() {
        if (logsList != null) {
            Map<String, Pattern> patterns = new HashMap<>();
            for (Map<String, String> magic : config.getMagic()) {
                patterns.put(magic.get("name"), Pattern.compile(magic.get("pattern"), Pattern.CASE_INSENSITIVE));
            }

            for (Object line : logsList) {
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
     * @param list List instance.
     */
    @SuppressWarnings("rawtypes")
    public void logResults(List list) {
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

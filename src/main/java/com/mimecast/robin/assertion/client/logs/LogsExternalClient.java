package com.mimecast.robin.assertion.client.logs;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.assertion.client.MatchExternalClient;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.config.assertion.external.logs.LogsExternalClientConfig;
import com.mimecast.robin.main.Config;
import com.mimecast.robin.util.Magic;
import com.mimecast.robin.util.Sleep;
import com.mimecast.robin.util.UIDExtractor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Logs external client.
 * <p>This provides a means to fetch the logs from a local file.
 */
public class LogsExternalClient extends MatchExternalClient {

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
     * @param config LogsExternalClient instance.
     * @return Self.
     */
    @Override
    public LogsExternalClient setConfig(BasicConfig config) {
        this.config = new LogsExternalClientConfig(config.getMap());

        // Update file path.
        setPath(((LogsExternalClientConfig) this.config).getLogPrecedence() + fileName);
        verifyNone = this.config.getVerifyNone();

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
     *
     * @throws AssertException Assertion exception.
     */
    @Override
    public void run() throws AssertException {
        if (!config.isEmpty()) {
            try {
                if (!verifyNone) {
                    compileVerify(); // Precompile verify patterns for performance.
                }
                List<String> data = getLogs(); // Get the logs for that UID and verify.

                // Verify no logs were found.
                if (verifyNone) {
                    log.info("AssertExternal logs verify none");

                } else if (!skip) {
                    runMatches(data);
                }
            } catch (Exception e) {
                throw new AssertException(e);
            }
        }
    }

    /**
     * Get logs.
     *
     * @return List of String.
     * @throws AssertException Assertion exception.
     */
    protected List<String> getLogs() throws AssertException {
        List<String> data = new ArrayList<>();

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
                    data.clear();
                    while ((line = br.readLine()) != null) {
                        String finalLine = line;
                        if ((patterns.isEmpty() && (uid == null || line.contains(uid))) || patterns.stream().allMatch(pattern -> pattern.matcher(finalLine).find())) {
                            data.add(line);
                        }
                    }

                } catch (IOException e) {
                    log.error("AssertExternal logs reading problems: {}", e.getMessage());
                    throw new AssertException("No logs found to assert against");
                }

                if (verifyNone) break;

                if (checkVerify(data)) {
                    log.debug("AssertExternal logs fetch verify success");
                    logResults(data);
                    break;
                }

                delay = config.getDelay() * 1000L; // Retry delay.
                log.info("AssertExternal logs fetch verify {}", (count < config.getRetry() - 1 ? "failure" : "attempts spent"));

                if (!assertVerifyFails) {
                    skip = true;
                    log.warn("Skipping");
                    return new ArrayList<>();
                }
            }

            if (data.isEmpty()) {
                if (!verifyNone) {
                    throw new AssertException("No logs found to assert against");
                }
            }
        }

        return data;
    }

    /**
     * Prepare grep patterns.
     */
    @SuppressWarnings("unchecked")
    protected List<Pattern> getGreps() {
        List<String> greps = ((List<Map<String, String>>) config.getListProperty("grep")).stream()
                .filter(map -> !map.containsKey("parameter") || !map.get("parameter").contains("v")) // `grep -v` skip patterns.
                .map(map -> {
                    String pattern = Magic.magicReplace(map.get("pattern"), connection.getSession());
                    return map.containsKey("parameter") && map.get("parameter").contains("E") ? pattern : Pattern.quote(pattern); // `grep -E` compile as is or escape.
                })
                .collect(Collectors.toList());

        return greps.stream()
                .map(string -> Pattern.compile(string, Pattern.CASE_INSENSITIVE))
                .collect(Collectors.toList());
    }
}

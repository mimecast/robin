package com.mimecast.robin.assertion.client.humio;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.assertion.client.ExternalClient;
import com.mimecast.robin.assertion.client.logs.LogsExternalClient;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.config.assertion.external.logs.LogsExternalClientConfig;
import com.mimecast.robin.util.Sleep;
import org.json.JSONArray;

/**
 * Humio external client.
 * <p>This provides a means to fetch the MTA logs via Functional Framework Service.
 *
 * @see <a href="https://library.humio.com/reference/api/search-api/">Humio Search API</a>
 */
public class HumioExternalClient extends LogsExternalClient {

    /**
     * Sets config instance.
     *
     * @param config ExternalClient instance.
     * @return Self.
     */
    @Override
    public ExternalClient setConfig(BasicConfig config) {
        this.config = new LogsExternalClientConfig(config.getMap());
        return this;
    }

    /**
     * Gets new HumioClient.
     *
     * @return HumioClient instance.
     */
    protected HumioClient getClient() {
        return new HumioClient(connection, config, transactionId);
    }

    /**
     * Find logs using given client if any.
     *
     * @throws AssertException Assertion exception.
     */
    @Override
    protected void findLogs() throws AssertException {
        HumioClient humioClient = getClient();

        if (connection.getServer().startsWith("127.") || connection.getServer().startsWith("local")) {
            super.findLogs();

        } else {
            long delay = config.getWait() > 0 ? config.getWait() * 1000L : 0L;
            for (int count = 0; count < config.getRetry(); count++) {
                Sleep.nap((int) delay);
                log.info("AssertExternal logs fetch attempt {} of {}", count + 1, config.getRetry());

                JSONArray logs = humioClient.run();
                if (logs != null && !logs.isEmpty()) {
                    logsList = logs.toList();
                    if (!verifyNone && verifyLogs()) {
                        log.debug("AssertExternal logs fetch verify success");
                        break;
                    }
                } else if (!verifyNone) {
                    log.debug("AssertExternal logs fetch got none");
                }

                if (verifyNone) break;

                delay = config.getDelay() * 1000L; // Retry delay.
                log.info("AssertExternal logs fetch verify {}", (count < config.getRetry() - 1 ? "failure" : "attempts spent"));
            }

            if (logsList == null || logsList.isEmpty()) {
                if (!verifyNone) {
                    throw new AssertException("No logs found to assert against");
                }
            }
        }
    }
}

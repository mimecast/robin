package com.mimecast.robin.assertion.client.humio;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.assertion.client.logs.LogsExternalClient;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.config.assertion.external.logs.LogsExternalClientConfig;
import com.mimecast.robin.util.Sleep;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

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
    public LogsExternalClient setConfig(BasicConfig config) {
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
     * Get logs.
     *
     * @return List of String.
     * @throws AssertException Assertion exception.
     */
    @Override
    protected List<String> getLogs() throws AssertException {
        HumioClient humioClient = getClient();
        List<String> data;

        if (connection.getServer().startsWith("127.") || connection.getServer().startsWith("local")) {
            data = super.getLogs();

        } else {
            data = new ArrayList<>();
            long delay = config.getWait() > 0 ? config.getWait() * 1000L : 0L;
            for (int count = 0; count < config.getRetry(); count++) {
                Sleep.nap((int) delay);
                log.info("AssertExternal logs fetch attempt {} of {}", count + 1, config.getRetry());

                JSONArray jsonArray = humioClient.run();
                if (jsonArray != null && !jsonArray.isEmpty()) {
                    jsonArray.toList().forEach(o -> {
                        if (o instanceof String) data.add((String) o);
                    });

                    if (!verifyNone && checkVerify(data)) {
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

            if (data.isEmpty()) {
                if (!verifyNone) {
                    throw new AssertException("No logs found to assert against");
                }
            }
        }

        return data;
    }
}

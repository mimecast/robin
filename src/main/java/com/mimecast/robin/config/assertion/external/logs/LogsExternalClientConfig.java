package com.mimecast.robin.config.assertion.external.logs;

import com.mimecast.robin.config.assertion.external.MatchExternalClientConfig;

import java.util.Map;

/**
 * Logs assertions config.
 */
public final class LogsExternalClientConfig extends MatchExternalClientConfig {

    /**
     * Constructs a new LogsExternalClientConfig instance.
     *
     * @param map Configuration map.
     */
    @SuppressWarnings("rawtypes")
    public LogsExternalClientConfig(Map map) {
        super(map);
    }

    /**
     * Get service name.
     *
     * @return String.
     */
    public String getService() {
        return hasProperty("serviceName") ? getStringProperty("serviceName") : "mta";
    }
}

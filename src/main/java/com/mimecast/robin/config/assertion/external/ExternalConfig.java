package com.mimecast.robin.config.assertion.external;

import com.mimecast.robin.config.BasicConfig;

import java.util.Map;

/**
 * External assertions config.
 *
 * <p>This should constructed using a map like the following:
 * <pre>
 *     {
 *       "wait": 10,
 *       "delay": 10,
 *       "retry": 3,
 *   }
 * </pre>
 *
 * <p>This may be present at envelope level only.
 */
public class ExternalConfig extends BasicConfig {

    /**
     * Constructs a new ExternalConfig instance.
     *
     * @param map Configuration map.
     */
    @SuppressWarnings("rawtypes")
    public ExternalConfig(Map map) {
        super(map);
    }

    /**
     * Gets initial wait in seconds.
     *
     * @return Initial wait.
     */
    public int getWait() {
        int wait = Math.toIntExact(getLongProperty("wait"));
        return Math.max(wait, 0);
    }

    /**
     * Gets retry delay in seconds.
     *
     * @return Retry delay.
     */
    public int getDelay() {
        int delay = Math.toIntExact(getLongProperty("delay"));
        return Math.max(delay, 0);
    }

    /**
     * Gets retry count.
     *
     * @return Retry count.
     */
    public int getRetry() {
        int retry = Math.toIntExact(getLongProperty("retry", 1L));
        return retry > 0 ? retry : 1;
    }
}

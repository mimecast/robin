package com.mimecast.robin.config.assertion;

import com.mimecast.robin.config.ConfigFoundation;

import java.util.List;
import java.util.Map;

/**
 * MTA assertions container.
 * <p>This should constructed using a map like the following:
 * <pre>
 *     "mta": {
 *         "delay": 5,
 *         "retry": 2,
 *         "match": [
 *             ["SPAMRESULT", "Action=Accept"]
 *         ]
 *     }
 * </pre>
 * <p>This may be present at both envelope level only.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
@SuppressWarnings("unchecked")
public class AssertMtaConfig extends ConfigFoundation {

    /**
     * Constructs a new AssertMtaConfig instance.
     *
     * @param map Configuration map.
     */
    public AssertMtaConfig(Map map) {
        super(map);
    }

    /**
     * Gets initial wait in seconds.
     *
     * @return Initial wait.
     */
    public int getWait() {
        int wait = Math.toIntExact(getLongProperty("wait"));
        return Math.max(wait, 2);
    }

    /**
     * Gets retry delay in seconds.
     *
     * @return Retry delay.
     */
    public int getDelay() {
        int delay = Math.toIntExact(getLongProperty("delay"));
        return Math.max(delay, 2);
    }

    /**
     * Gets retry count.
     *
     * @return Retry count.
     */
    public int getRetry() {
        return Math.toIntExact(getLongProperty("retry"));
    }

    /**
     * Gets regex rules to verify logs.
     *
     * @return List.
     */
    public List<String> getVerify() {
        return getListProperty("verify");
    }

    /**
     * Gets regex rules to match against.
     *
     * @return List in list.
     */
    public List<List<String>> getMatch() {
        return getListProperty("match");
    }

    /**
     * Gets regex rules to NOT match against.
     *
     * @return List in list.
     */
    public List<List<String>> getRefuse() {
        return getListProperty("refuse");
    }
}

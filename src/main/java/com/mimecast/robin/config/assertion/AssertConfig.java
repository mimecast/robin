package com.mimecast.robin.config.assertion;

import com.mimecast.robin.assertion.client.ExternalClient;
import com.mimecast.robin.config.ConfigFoundation;
import com.mimecast.robin.main.Factories;

import java.util.List;
import java.util.Map;

/**
 * Assertions container.
 * <p>This should constructed using a map like the following:
 * <pre>
 *     "assertions": {
 *         "smtp": [
 *             ["MAIL", "250 Sender OK"],
 *             ["RCPT", "250 Recipient OK"],
 *             ["DATA", "^250"],
 *             ["DATA", "Received OK"]
 *         ],
 *         "external": {
 *             "delay": 5,
 *             "retry": 2,
 *             "match": [
 *                 ["SPAMRESULT", "Action=Accept"]
 *             ]
 *         }
 *     }
 * </pre>
 * <p>This may be present at both session and envelope level.
 * <p>SMTP assertions are done directly over the SMTP transactions.
 * <p>External assertions require a client for fetching the logs.
 *
 * @see ExternalClient
 * @see Factories;
 * @see AssertExternalConfig
 */
@SuppressWarnings("unchecked")
public class AssertConfig extends ConfigFoundation {

    /**
     * Constructs a new AssertConfig instance.
     */
    public AssertConfig() {
        super();
    }

    /**
     * Constructs a new AssertConfig instance with given configuration map.
     *
     * @param map Configuration map.
     */
    @SuppressWarnings("rawtypes")
    public AssertConfig(Map map) {
        super(map);
    }

    /**
     * Gets SMTP assertion list.
     *
     * @return List in list.
     */
    public List<List<String>> getSmtp() {
        return getListProperty("smtp");
    }

    /**
     * Gets external assertion configuration instance.
     *
     * @return AssertExternalConfig instance.
     */
    public AssertExternalConfig getExternal(String name) {
        return new AssertExternalConfig(getMapProperty(name));
    }
}

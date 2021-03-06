package com.mimecast.robin.config.assertion;

import com.mimecast.robin.assertion.client.ExternalClient;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.config.ConfigFoundation;
import com.mimecast.robin.main.Factories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Assertions container.
 *
 * <p>SMTP assertions may be present at both session and envelope level.
 * <br>They are done directly over the SMTP transactions.
 *
 * <p>External assertions require a client for fetching the logs.
 *
 * @see ExternalClient
 * @see Factories
 * @see BasicConfig
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
     * @return List of BasicConfig instance.
     */
    public List<BasicConfig> getExternal() {
        List<BasicConfig> external = new ArrayList<>();
        for (Object map : getListProperty("external")) {
            if (map instanceof Map) {
                external.add(new BasicConfig((Map) map));
            }
        }

        return external;
    }
}

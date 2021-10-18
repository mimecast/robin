package com.mimecast.robin.config.assertion.external.logs;

import com.mimecast.robin.config.assertion.external.MatchExternalClientConfig;
import com.mimecast.robin.smtp.connection.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Logs assertions config.
 *
 * <p>This should be constructed using a map like the following:
 * <pre>
 *     {
 *       "type": "logs",
 *       "wait": 10,
 *       "delay": 10,
 *       "retry": 3,
 *       "grep": [
 *         {
 *           "parameter": "-E",
 *           "pattern": "{$uid}"
 *         },
 *         {
 *           "pattern": "SmtpThread"
 *         },
 *         {
 *           "parameter": "-vE",
 *           "pattern": "DEBUG|TRACE"
 *         }
 *       ],
 *       "verify": ["MTAJNLPROCSUMMARY"],
 *       "match": [
 *         ["MTARCPT", "Dir=Jnl"],
 *       ],
 *       "refuse": [
 *          ["java.lang.NullPointerException"]
 *       ]
 *   }
 * </pre>
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
     * Gets grep.
     *
     * @param connection    Connection instance.
     * @param transactionId Transaction ID.
     * @return List of Map of String, String.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> getGrep(Connection connection, int transactionId) {
        List<Map<String, String>> grep = new ArrayList<>();

        for (Map<String, String> map : (List<Map<String, String>>) getListProperty("grep")) {
            map.put("pattern", magicReplace(map.get("pattern"), connection, transactionId));
            grep.add(map);
        }

        return grep;
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

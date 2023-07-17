package com.mimecast.robin.config.assertion.external;

import com.mimecast.robin.smtp.connection.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Assertions config extension for matching calls.
 */
public class MatchExternalClientConfig extends ExternalConfig {

    /**
     * Constructs a new MatchExternalClientConfig instance.
     *
     * @param map Configuration map.
     */
    @SuppressWarnings("rawtypes")
    public MatchExternalClientConfig(Map map) {
        super(map);
    }

    /**
     * Gets regex rules to verify logs.
     *
     * @return List.
     */
    @SuppressWarnings("unchecked")
    public List<String> getVerify() {
        return getListProperty("verify");
    }

    /**
     * Gets regex rules to NOT match against.
     *
     * @return List in list.
     */
    @SuppressWarnings("unchecked")
    public List<List<String>> getRefuse() {
        return getListProperty("refuse");
    }

    /**
     * Gets regex rules to NOT match against.
     *
     * @return List in list.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> getMagic() {
        return getListProperty("magic");
    }

    /**
     * Gets regex rules to match against.
     *
     * @param connection    Connection instance.
     * @param transactionId Transaction ID.
     * @return List in list.
     */
    @SuppressWarnings("unchecked")
    public List<List<String>> getMatch(Connection connection, int transactionId) {
        List<List<String>> results = new ArrayList<>();

        List<List<String>> match = getListProperty("match");
        for (List<String> list : match) {
            results.add(
                    list.stream()
                            .map(s -> connection.getSession().transactionMagicReplace(s, connection, transactionId))
                            .collect(Collectors.toList())
            );
        }

        return results;
    }
}

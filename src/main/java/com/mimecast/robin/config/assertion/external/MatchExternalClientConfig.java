package com.mimecast.robin.config.assertion.external;

import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.session.Session;
import com.mimecast.robin.util.Magic;

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
     * Gets verify no data found.
     *
     * @return Boolean.
     */
    public boolean getVerifyNone() {
        return getBooleanProperty("verifyNone", false);
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
     * @param session       Session instance.
     * @return List in list.
     */
    @SuppressWarnings("unchecked")
    public List<List<String>> getMatch(Session session) {
        List<List<String>> results = new ArrayList<>();

        List<List<String>> match = getListProperty("match");
        for (List<String> list : match) {
            results.add(
                    list.stream()
                            .map(s -> Magic.magicReplace(s, session))
                            .collect(Collectors.toList())
            );
        }

        return results;
    }

    /**
     * Evaluates execution condition.
     *
     * @param connection    Connection instance.
     * @return Boolean.
     */
    public boolean checkCondition(Connection connection) {
        String condition = getStringProperty("condition");

        if (condition != null) {
            condition = Magic.magicReplace(condition, connection.getSession(), true);

            if (condition.contains("==")) {
                String[] splits = condition.split("==", 2);
                return splits.length == 2 && splits[0].trim().equals(splits[1].trim());
            }

            if (condition.contains("!=")) {
                String[] splits = condition.split("!=", 2);
                return splits.length == 2 && !splits[0].trim().equals(splits[1].trim());
            }

            return false;
        }

        return true;
    }
}

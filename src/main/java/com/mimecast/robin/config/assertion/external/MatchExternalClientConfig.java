package com.mimecast.robin.config.assertion.external;

import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.util.UIDExtractor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Assertions config extension for matching calls.
 */
public class MatchExternalClientConfig extends ExternalConfig {

    /**
     * Magic variable pattern.
     */
    protected final static Pattern magicVariablePattern = Pattern.compile("\\{\\$([a-z0-9]+)(\\[([0-9]+)]\\[([a-z0-9]+)])?}", Pattern.CASE_INSENSITIVE);

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
                            .map(s -> magicReplace(s, connection, transactionId))
                            .collect(Collectors.toList())
            );
        }

        return results;
    }

    /**
     * Magic replace string.
     *
     * @param magicString   Magic string.
     * @param connection    Connection instance.
     * @param transactionId Transaction ID.
     * @return String.
     */
    @SuppressWarnings("unchecked")
    public String magicReplace(String magicString, Connection connection, int transactionId) {
        Matcher matcher = magicVariablePattern.matcher(magicString);
        Map<String, String> magic = connection.getMagic(transactionId);

        // Add UID to magic.
        if (!magic.containsKey("uid")) {
            String uid = UIDExtractor.getUID(connection, transactionId);
            if (StringUtils.isNotBlank(uid)) {
                magic.put("uid", uid);
            }
        }

        // Add yyyyMMdd date.
        if (!magic.containsKey("yymd")) {
            if (transactionId >= 0) {
                magic.put("yymd", connection.getSession().getEnvelopes().get(transactionId).getYymd());
            }
        }

        while (matcher.find()) {
            String magicVariable = matcher.group();

            String magicName = matcher.group(1);
            String resultColumn = matcher.group(4);
            String value = null;

            // Magic session variables.
            if (connection.getSession().isMagic(magicName)) {
                value = (String) connection.getSession().getMagic(magicName);
            }

            // Magic transaction variables.
            if (resultColumn == null && magic.containsKey(magicName)) {
                value = magic.get(magicName);
            }

            // Saved results
            if (resultColumn != null && connection.getSession().getSavedResults().containsKey(magicName)) {
                int resultRow = Integer.parseInt(matcher.group(3));

                if (connection.getSession().getSavedResults().get(magicName) != null &&
                        connection.getSession().getSavedResults().get(magicName).get(resultRow) != null) {

                    value = String.valueOf(((Map<String, String>) connection.getSession().getSavedResults().get(magicName).get(resultRow)).get(resultColumn));
                }
            }

            magicString = magicString.replace(magicVariable, value == null ? "null" : value);
        }

        return magicString;
    }
}

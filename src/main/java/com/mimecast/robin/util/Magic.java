package com.mimecast.robin.util;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.io.MagicInputStream;
import com.mimecast.robin.smtp.session.Session;
import com.mimecast.robin.smtp.transaction.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Magic processors.
 */
public class Magic {
    private static final Logger log = LogManager.getLogger(Magic.class);

    /**
     * Magic variable pattern.
     */
    protected final static Pattern magicVariablePattern = Pattern.compile("\\{([a-z]+)?(\\([a-z0-9-_.:;|]+\\))?\\$([a-z0-9-_.]+)(\\[([0-9?]+)](\\[([a-z0-9]+)])?)?}", Pattern.CASE_INSENSITIVE);

    /**
     * Transaction response pattern.
     */
    private static final Pattern transactionPattern = Pattern.compile("(250.*)\\s\\[[a-z0-9\\-_]+\\.[a-z]+([0-9]+)?]", Pattern.CASE_INSENSITIVE);

    /**
     * Puts magic variables in session.
     *
     * @param session Session instance.
     */
    @SuppressWarnings("unchecked")
    public static void putMagic(Session session) {
        session.putMagic("robinUid", session.getUID());
        session.putMagic("robinYymd", new SimpleDateFormat("yyyyMMdd").format(new Date()));
        session.putMagic("robinDate", new SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z", Config.getProperties().getLocale()).format(new Date()));

        // Add magic properties.
        for (Map.Entry<String, Object> entry : Config.getProperties().getMap().entrySet()) {
            if (entry.getValue() instanceof String) {
                session.putMagic(entry.getKey(), entry.getValue());

            } else if (entry.getValue() instanceof List) {
                session.putMagic(entry.getKey(), ((List) entry.getValue()).stream()
                        .filter(o -> o instanceof String)
                        .collect(Collectors.toList()));
            }
        }

        // Add magic arguments.
        List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments()
                .stream()
                .filter(s -> s.startsWith("-D"))
                .map(s -> s.replace("-D", "").replaceAll("=.*", ""))
                .collect(Collectors.toList());

        for (String key : args) {
            session.putMagic(key, Config.getProperties().getStringProperty(key));
        }
    }

    /**
     * Session magic replace.
     *
     * @param magicString Magic string.
     * @param session     Session instance.
     * @return Map of String, Object.
     */
    public static String magicReplace(String magicString, Session session) {
        return magicReplace(magicString, session, false);
    }

    /**
     * Session magic replace with optional null string.
     *
     * @param magicString Magic string.
     * @param session     Session instance.
     * @param nullString  Force null string for null values.
     * @return Map of String, Object.
     */
    @SuppressWarnings("unchecked")
    public static String magicReplace(String magicString, Session session, boolean nullString) {

        Matcher matcher = magicVariablePattern.matcher(magicString);

        while (matcher.find()) {
            String magicVariable = matcher.group();

            String magicFunction = matcher.group(1);
            String magicArgs = matcher.group(2);
            if (magicArgs != null) {
                magicArgs = magicArgs.replaceAll("[()]", "");
            }
            String magicName = matcher.group(3);
            String resultColumn = matcher.group(7);
            String value = null;

            // Magic variables.
            if (session.hasMagic(magicName)) {
                if (session.getMagic(magicName) instanceof String) {
                    value = (String) session.getMagic(magicName);

                } else if (session.getMagic(magicName) instanceof List) {
                    List<String> values = (List<String>) ((List) session.getMagic(magicName)).stream()
                            .filter(v -> v instanceof String)
                            .collect(Collectors.toList());

                    int key = "?".equals(matcher.group(4)) ?
                            Random.no(values.size()) - 1 :
                            Integer.parseInt(matcher.group(4));

                    value = values.get(key);
                }
            }

            // Saved results.
            if (resultColumn != null && session.getSavedResults().containsKey(magicName)) {
                int resultRow = Integer.parseInt(matcher.group(4));

                if (session.getSavedResults().get(magicName) != null &&
                        session.getSavedResults().get(magicName).get(resultRow) != null) {

                    value = String.valueOf(((Map<String, String>) session.getSavedResults().get(magicName).get(resultRow)).get(resultColumn));
                }
            }

            // Magic functions.
            if (magicFunction != null && value != null) {
                if ("dateToMillis".equals(magicFunction)) {
                    value = dateToMillis(value);
                } else if ("millisToDate".equals(magicFunction)) {
                    value = millisToDate(value);
                } else if ("toLowerCase".equals(magicFunction)) {
                    value = value.toLowerCase();
                } else if ("toUpperCase".equals(magicFunction)) {
                    value = value.toUpperCase();
                } else if ("patternQuote".equals(magicFunction)) {
                    value = Pattern.quote(value);
                } else if ("strip".equals(magicFunction)) {
                    if (magicArgs != null) {
                        value = value.replaceAll(magicArgs, "");
                    } else {
                        log.warn("Magic strip function requires an argument got: {}", magicArgs);
                    }
                } else if ("replace".equals(magicFunction)) {
                    if (magicArgs != null && magicArgs.contains("|")) {
                        String[] replaceArgs = magicArgs.split("\\|", 2);
                        value = value.replaceAll(Pattern.quote(replaceArgs[0]), replaceArgs[1]);
                    } else {
                        log.warn("Magic replace function requires two arguments separated by | but got: {}", magicArgs);
                    }
                }
            }

            if (value != null) {
                magicString = magicString.replace(magicVariable, value);
            } else if (nullString) {
                magicString = magicString.replace(magicVariable, "null");
            }
        }

        return magicString;
    }

    /**
     * Puts magic variables for transaction in session.
     *
     * @param transactionId Transaction id.
     * @param session       Session instance.
     */
    public static void putTransactionMagic(int transactionId, Session session) {
        session.putMagic("transactionId", String.valueOf(transactionId));

        if (!session.getSessionTransactionList().getEnvelopes().isEmpty() && transactionId >= 0) {

            // Put transaction (SMTP DATA/BDAT response).
            Transaction transaction = session.getSessionTransactionList().getEnvelopes().get(transactionId).getData();
            if (transaction != null && transaction.getResponse().startsWith("250 ")) {
                Matcher m = transactionPattern.matcher(transaction.getResponse());
                if (m.find()) {
                    String group = m.group(1);
                    session.putMagic("transactionResponse", group);
                    session.putMagic("transactionid", group); // TODO Deprecate.
                }
            }

            // Put UID.
            session.putMagic("transactionUid", UIDExtractor.getUID(new Connection(session), transactionId));
            session.putMagic("uid", UIDExtractor.getUID(new Connection(session), transactionId)); // TODO Deprecate.
        }
    }

    /**
     * Simple date format instance.
     */
    protected final static SimpleDateFormat millisDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    /**
     * Converts readable date to epoch millis.
     *
     * @param dateString String of date in format: yyyyMMddHHmmssSSS
     * @return String.
     */
    public static String dateToMillis(String dateString) {
        try {
            return String.valueOf(millisDateFormat.parse(dateString).getTime());
        } catch (ParseException e) {
            log.error("Unable to convert date string to millis: {}", e.getMessage());
        }

        return dateString;
    }

    /**
     * Converts epoch millis to readable date.
     *
     * @param millisString String of epoch millis.
     * @return String.
     */
    public static String millisToDate(String millisString) {
        return millisDateFormat.format(new Date(Long.parseLong(millisString)));
    }

    /**
     * Put magic variables for envelope in session.
     *
     * @param transactionId Transaction id.
     * @param session       Session instance.
     */
    public static void putEnvelopeMagic(int transactionId, Session session) {
        MessageEnvelope envelope = session.getEnvelopes().get(transactionId);

        session.putMagic("yymd", envelope.getYymd());
        session.putMagic("msgid", envelope.getMessageId());
        session.putMagic("date", envelope.getDate());
        session.putMagic("mailfrom", envelope.getMail());
        session.putMagic("rcptto", envelope.getRcpt());

        envelope.getHeaders().forEach((key, value) -> session.putMagic("headers[" + key + "]", value));
    }

    /**
     * Envelope magic replace.
     *
     * @param string   String.
     * @param envelope MessageEnvelope instance.
     * @return Byte array.
     */
    public static String envelopeMagicReplace(String string, MessageEnvelope envelope) {
        return readLines(new MagicInputStream(new ByteArrayInputStream(string.getBytes()), envelope));
    }

    /**
     * Stream magic replace.
     *
     * @param string String.
     * @return Byte array.
     */
    public static String streamMagicReplace(String string) {
        return readLines(new MagicInputStream(new ByteArrayInputStream(string.getBytes())));
    }

    /**
     * Read lines from MagicInputStream instance.
     *
     * @param magicInputStream MagicInputStream instance.
     * @return String.
     */
    protected static String readLines(MagicInputStream magicInputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            byte[] line;
            while ((line = magicInputStream.readLine()) != null) {
                stringBuilder.append(new String(line));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return stringBuilder.toString();
    }
}

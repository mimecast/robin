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
    protected final static Pattern magicVariablePattern = Pattern.compile("\\{([a-z]+)?\\$([a-z0-9]+)(\\[([0-9]+)]\\[([a-z0-9]+)])?}", Pattern.CASE_INSENSITIVE);

    /**
     * Transaction response pattern.
     */
    private static final Pattern transactionPattern = Pattern.compile("(250.*)\\s\\[[a-z0-9\\-_]+\\.[a-z]+([0-9]+)?]", Pattern.CASE_INSENSITIVE);

    /**
     * Puts magic variables in session.
     *
     * @param session Session instance.
     */
    public static void putMagic(Session session) {
        session.putMagic("uid", session.getUID());
        session.putMagic("yymd", new SimpleDateFormat("yyyyMMdd").format(new Date()));

        // Add magic properties.
        for (Map.Entry<String, Object> entry : Config.getProperties().getMap().entrySet()) {
            if (entry.getValue() instanceof String) {
                session.putMagic(entry.getKey(), entry.getValue());
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
        if (magicString != null) {
            for (String key : session.getMagic().keySet()) {
                if (magicString.contains("{$" + key + "}")) {
                    Object val = session.getMagic(key);
                    if (val instanceof String) {
                        magicString = magicString.replaceAll("\\{\\$" + key + "}", Matcher.quoteReplacement((String) val));
                    }
                }
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
        if (!session.getSessionTransactionList().getEnvelopes().isEmpty() && transactionId >= 0) {

            // Put transaction (SMTP DATA/BDAT response).
            Transaction transaction = session.getSessionTransactionList().getEnvelopes().get(transactionId).getData();
            if (transaction != null && transaction.getResponse().startsWith("250 ")) {
                Matcher m = transactionPattern.matcher(transaction.getResponse());
                if (m.find()) {
                    session.putMagic("transactionid", m.group(1));
                }
            }

            // Put UID.
            session.putMagic("uid", UIDExtractor.getUID(new Connection(session), transactionId));
        }
    }

    /**
     * Transaction magic replace.
     *
     * @param magicString   Magic string.
     * @param session       Session instance.
     * @param transactionId Transaction ID.
     */
    @SuppressWarnings("unchecked")
    public static String transactionMagicReplace(String magicString, Session session, int transactionId) {
        Matcher matcher = magicVariablePattern.matcher(magicString);

        while (matcher.find()) {
            String magicVariable = matcher.group();

            String magicfunction = matcher.group(1);
            String magicName = matcher.group(2);
            String resultColumn = matcher.group(5);
            String value = null;

            // Magic variables.
            if (session.hasMagic(magicName)) {
                value = (String) session.getMagic(magicName);
            }

            // Saved results
            if (resultColumn != null && session.getSavedResults().containsKey(magicName)) {
                int resultRow = Integer.parseInt(matcher.group(4));

                if (session.getSavedResults().get(magicName) != null &&
                        session.getSavedResults().get(magicName).get(resultRow) != null) {

                    value = String.valueOf(((Map<String, String>) session.getSavedResults().get(magicName).get(resultRow)).get(resultColumn));
                }
            }

            // Magic functions.
            if (value != null) {
                if ("dateToMillis".equals(magicfunction)) {
                    value = dateToMillis(value);
                }
                if ("millisToDate".equals(magicfunction)) {
                    value = millisToDate(value);
                }
                if ("toLowerCase".equals(magicfunction)) {
                    value = value.toLowerCase();
                }
                if ("toUpperCase".equals(magicfunction)) {
                    value = value.toUpperCase();
                }
            }

            magicString = magicString.replace(magicVariable, value == null ? "null" : value);
        }

        return magicString;
    }

    /**
     * Simple date format instance.
     */
    protected final static SimpleDateFormat millisDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    /**
     * Converts readable date to epoch millis.
     *
     * @param dateString String of date in format: yyyyMMddHHmmssSSS
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
     * @param magicBytes Byte array.
     * @param envelope   MessageEnvelope instance.
     * @return Byte array.
     */
    public static byte[] envelopeMagicReplace(byte[] magicBytes, MessageEnvelope envelope) {
        StringBuilder magicHtml = new StringBuilder();
        try {
            MagicInputStream magicInputStream = new MagicInputStream(new ByteArrayInputStream(magicBytes), envelope);

            byte[] line;
            while ((line = magicInputStream.readLine()) != null) {
                magicHtml.append(new String(line));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return magicHtml.toString().getBytes();
    }
}

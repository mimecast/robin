package com.mimecast.robin.smtp.io;

import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.util.Random;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Input stream with magic variable replacement capability.
 *
 * <p>This implements line reading InputStream.
 * <p>It finds and replaces magic tags in lines read given MessageEnvelope provided.
 * <p>It uses LineInputStream to do the actual line reading.
 *
 * <p><b>Will break binary integrity on replaced lines, to be used in string lines only.</b>
 *
 * @see LineInputStream
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class MagicInputStream extends LineInputStream {

    /**
     * MessageEnvelope instance.
     */
    MessageEnvelope envelope;

    /**
     * Smart magic tags patterns.
     */
    private static final Pattern patternRandCh = Pattern.compile("\\{\\$randch([0-9]*)}", Pattern.CASE_INSENSITIVE);
    private static final Pattern patternRandNo = Pattern.compile("\\{\\$randno([0-9]*)}", Pattern.CASE_INSENSITIVE);

    /**
     * Simple magic tags patterns.
     */
    private static final Map<String, Pattern> simpleTags = new HashMap<>();

    static {
        simpleTags.put("{$msgid}", Pattern.compile("\\{\\$msgid}", Pattern.CASE_INSENSITIVE));
        simpleTags.put("{$date}", Pattern.compile("\\{\\$date}", Pattern.CASE_INSENSITIVE));
        simpleTags.put("{$yymd}", Pattern.compile("\\{\\$yymd}", Pattern.CASE_INSENSITIVE));
        simpleTags.put("{$year}", Pattern.compile("\\{\\$year}", Pattern.CASE_INSENSITIVE));
        simpleTags.put("{$mailfrom}", Pattern.compile("\\{\\$mailfrom}", Pattern.CASE_INSENSITIVE));
        simpleTags.put("{$mail}", Pattern.compile("\\{\\$mail}", Pattern.CASE_INSENSITIVE));
        simpleTags.put("{$rcptto}", Pattern.compile("\\{\\$rcptto}", Pattern.CASE_INSENSITIVE));
        simpleTags.put("{$rcpt}", Pattern.compile("\\{\\$rcpt}", Pattern.CASE_INSENSITIVE));
        simpleTags.put("{$headers", Pattern.compile("\\{\\$headers(\\[[a-z0-9-]+])?}", Pattern.CASE_INSENSITIVE));
    }

    /**
     * Constructs a new MagicInputStream instance with given MessageEnvelope.
     *
     * @param in       InputStream instance.
     * @param envelope MessageEnvelope instance.
     */
    public MagicInputStream(InputStream in, MessageEnvelope envelope) {
        this(in);
        this.envelope = envelope;
    }

    /**
     * Constructs a new MagicInputStream instance.
     *
     * @param in InputStream instance.
     */
    public MagicInputStream(InputStream in) {
        super(in);
    }

    /**
     * Read line as byte array.
     *
     * @return Byte array.
     * @throws IOException Unable to read.
     */
    @Override
    public byte[] readLine() throws IOException {
        return doMagic(super.readLine());
    }

    /**
     * Replace magic variables in line bytes.
     *
     * @param lineBytes Byte array.
     * @return Byte array.
     */
    byte[] doMagic(byte[] lineBytes) {
        if (lineBytes != null && envelope != null) {
            boolean changed = false;
            String tag = new String(lineBytes).toLowerCase();
            String line = new String(lineBytes);

            if (tag.contains("{$randch")) {
                String random = randCh(line);
                changed = !randCh(line).equals(line);
                line = random;
            }

            if (tag.contains("{$randno")) {
                String random = randNo(line);
                changed = !randNo(line).equals(line);
                line = random;
            }

            String magic = doMagic(tag, line);
            if (!line.equals(magic)) {
                changed = true;
                line = magic;
            }

            if (changed) {
                lineBytes = line.getBytes();
            }
        }

        return lineBytes;
    }

    /**
     * Replace simple magic tags in line string.
     *
     * @param tag  Magic tag.
     * @param line Line string.
     * @return Line string.
     */
    String doMagic(String tag, String line) {
        for (Map.Entry<String, Pattern> entry : simpleTags.entrySet()) {
            if (tag.contains(entry.getKey())) {

                String key = entry.getKey();
                Matcher matcher = entry.getValue().matcher(tag);
                if (matcher.find()) {
                    key = matcher.group();
                }
                String replacement = getReplacement(key);
                if (replacement != null) {
                    line = entry.getValue().matcher(line).replaceAll(replacement);
                }
            }
        }

        return line;
    }

    /**
     * Gets replacement for simple magic tags.
     *
     * @param key Tag key.
     * @return Value string.
     */
    String getReplacement(String key) {
        String param = null;
        if (key.contains("[") && key.contains("]")) {
            String[] splits = key.replace("]}", "").split("\\[");
            param = splits[1].toLowerCase();
            key = key.replace("[" + param + "]", "");
        }

        if (envelope != null) {
            switch (key.toLowerCase()) {
                case "{$msgid}":
                    return envelope.getMessageId();

                case "{$date}":
                    return envelope.getDate();

                case "{$yymd}":
                    return envelope.getYymd();

                case "{$year}":
                    return envelope.getYear();

                case "{$mailfrom}":
                case "{$mail}":
                    return envelope.getMailFrom();

                case "{$rcptto}":
                case "{$rcpt}":
                    return envelope.getRcptTo();

                case "{$headers}":
                    if (param != null) {
                        return envelope.getHeaders().get(param);
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        envelope.getHeaders().forEach((k, v) -> stringBuilder.append(StringUtils.capitalize(k)).append(": ").append(v).append("\r\n"));
                        return stringBuilder.toString();
                    }

                default:
                    return "";
            }
        }

        return "";
    }

    /**
     * Random character string.
     *
     * @param line Line string.
     * @return Value string.
     */
    String randCh(String line) {
        Matcher matcher = patternRandCh.matcher(line);
        int rnd = 20;

        if (matcher.find()) {
            String len = matcher.group(1);
            if (StringUtils.isNotBlank(len)) {
                rnd = Integer.parseInt(matcher.group(1));
            }

            return matcher.replaceAll(Random.ch(rnd));
        }

        return line;
    }

    /**
     * Random number string.
     *
     * @param line Line string.
     * @return Value string.
     */
    String randNo(String line) {
        Matcher matcher = patternRandNo.matcher(line);
        int rnd = 10;

        if (matcher.find()) {
            String len = matcher.group(1);
            if (StringUtils.isNotBlank(len)) {
                rnd = Integer.parseInt(matcher.group(1));
            }

            return matcher.replaceAll(Integer.toString(Random.no(rnd)));
        }

        return line;
    }
}

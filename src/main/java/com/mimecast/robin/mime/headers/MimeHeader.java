package com.mimecast.robin.mime.headers;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.internet.HeaderTokenizer;
import javax.mail.internet.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * MIME header container.
 */
public class MimeHeader {
    private static final Logger log = LogManager.getLogger(MimeHeader.class);

    /**
     * Header name.
     */
    protected final String name;

    /**
     * Header value.
     */
    protected final String value;

    /**
     * Header clean value.
     */
    protected String cleanValue;

    /**
     * Header parameters.
     */
    protected final Map<String, String> parameters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * Constructs a new MimeHeader instance with given header string.
     *
     * @param header Complete header.
     */
    public MimeHeader(String header) {
        String[] splits = header.trim().split(":", 2);
        this.name = splits.length > 0 ? splits[0].trim() : "x-unknown";
        this.value = splits.length > 1 ? splits[1].trim() : "";
    }

    /**
     * Constructs a new MimeHeader instance with given name and value.
     *
     * @param name  Header name.
     * @param value Header value.
     */
    public MimeHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Gets header name.
     *
     * @return Header name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets header value.
     *
     * @return Header value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets header clean value.
     *
     * @return Header value.
     */
    public String getCleanValue() {
        parseValue();
        return cleanValue;
    }

    /**
     * Gets header parameter with given name.
     *
     * @param name Parameter name.
     * @return Header value.
     */
    public String getParameter(String name) {
        parseValue();
        return parameters.get(name);
    }

    /**
     * Gets header parameter with given name.
     */
    public void parseValue() {
        if (cleanValue == null) {
            List<String> tokens = new ArrayList<>();

            try {
                HeaderTokenizer tokenizer = new HeaderTokenizer(value, " ,;:\"'\t=\\", true);
                HeaderTokenizer.Token htt = tokenizer.next();
                while (HeaderTokenizer.Token.EOF != htt.getType()) {
                    String tokenValue = htt.getValue().trim();

                    // Save clean value.
                    if (cleanValue == null) {
                        cleanValue = tokenValue;
                        htt = tokenizer.next();
                        continue;
                    }

                    // Save parameter.
                    if (StringUtils.isNotBlank(tokenValue) &&
                            !tokenValue.equals(",") &&
                            !tokenValue.equals(";") &&
                            !tokenValue.equals("'")) {

                        tokens.add(tokenValue);
                    }
                    htt = tokenizer.next();
                }
            } catch (ParseException e) {
                log.error("Parse exception: {}", e.getMessage());
            }

            getParameters(tokens);
        }
    }

    /**
     * Gets parameters from tokens.
     *
     * @param tokens List of string tokens.
     */
    private void getParameters(List<String> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (i > 0 && token.equals("=")) {
                String headerName = tokens.get(i - 1);

                if (tokens.size() > i + 1) {
                    String headerValue = tokens.get(i + 1);
                    parameters.put(headerName, headerValue);
                }
            }
        }
    }

    /**
     * Returns a string representation of the header by
     * combinging name and value separated by collon and space.
     *
     * @return Header string.
     */
    @Override
    public String toString() {
        return name + ": " + value + "\r\n";
    }
}

package com.mimecast.robin.mime.headers;

import org.apache.commons.lang3.StringUtils;

import javax.mail.internet.HeaderTokenizer;
import javax.mail.internet.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MIME header container.
 */
public class MimeHeader {

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
    protected final Map<String, String> parameters = new HashMap<>();

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
     * @throws ParseException Invalid heder value.
     */
    public String getCleanValue() throws ParseException {
        parseValue();
        return cleanValue;
    }

    /**
     * Gets header parameter with given name.
     *
     * @param name Parameter name.
     * @return Header value.
     * @throws ParseException Invalid heder value.
     */
    public String getParameter(String name) throws ParseException {
        parseValue();
        return parameters.get(name);
    }

    /**
     * Gets header parameter with given name.
     *
     * @throws ParseException Invalid heder value.
     */
    public void parseValue() throws ParseException {
        if (cleanValue == null) {
            List<String> tokens = new ArrayList<>();

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
                String name = tokens.get(i - 1);

                if (tokens.size() > i + 1) {
                    String value = tokens.get(i + 1);
                    parameters.put(name, value);
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

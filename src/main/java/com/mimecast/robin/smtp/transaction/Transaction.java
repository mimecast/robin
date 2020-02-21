package com.mimecast.robin.smtp.transaction;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Transaction.
 *
 * <p>This provides a container for SMTP transaction details.
 */
public class Transaction {
    private static final Logger log = LogManager.getLogger(Transaction.class);

    /**
     * Records the SMTP command of this transaction.
     */
    private final String command;

    /**
     * Records the SMTP command payload if any.
     */
    private String payload;

    /**
     * Records the SMTP response if any.
     */
    private String response;

    /**
     * Records the SMTP response is in error.
     */
    private boolean error;

    /**
     * Constructor.
     *
     * @param command Command string.
     */
    public Transaction(String command) {
        this.command = command;
    }

    /**
     * Gets the SMTP command of this transaction.
     *
     * @return Command string.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Sets the SMTP command payload.
     *
     * @param payload Payload string.
     * @return Self.
     */
    public Transaction setPayload(String payload) {
        this.payload = payload;
        return this;
    }

    /**
     * Gets the SMTP command payload.
     *
     * @return Payload string.
     */
    public String getPayload() {
        return payload;
    }

    /**
     * Sets the SMTP command response.
     *
     * @param response Response string.
     * @return Self.
     */
    public Transaction setResponse(String response) {
        this.response = response.trim();
        return this;
    }

    /**
     * Gets the SMTP command response.
     *
     * @return Response string.
     */
    public String getResponse() {
        return response;
    }

    /**
     * Gets the SMTP command response code (first three characters).
     * <p>Returns 102 if response length too short.
     *
     * @return Response code string.
     */
    public String getResponseCode() {
        String code = "102";

        try {
            if (response != null && response.length() > 3) {
                String temp = response.substring(0, 3);
                if (Integer.parseInt(temp) > 0) {
                    code = temp;
                }
            }
        } catch (NumberFormatException e) {
            log.info("Unable to parse integer: {}", e.getMessage());
        }

        return code;
    }

    /**
     * Gets the SMTP command response as a single line string.
     *
     * @return Response string.
     */
    public String getResponseString() {
        return response
                .replaceAll(getResponseCode() + "-", "") // Remove the preceding code followed by a dash.
                .replaceAll(getResponseCode() + " ", "") // Remove the preceding code followed by a space.
                .replaceAll("\r", " ") // Replace CR with space.
                .replaceAll("\n", " ") // Replace LF with space.
                .replaceAll("\\s+", " ") // Dedupe whitespaces.
                .trim()
                ;
    }

    /**
     * Sets the SMTP command error.
     *
     * @param error Error enablement.
     * @return Self
     */
    public Transaction setError(boolean error) {
        this.error = error;
        return this;
    }

    /**
     * Is the SMTP command in error.
     *
     * @return Is error boolean.
     */
    public boolean isError() {
        return error;
    }

    /**
     * Transactions as string.
     *
     * @return Transaction string.
     */
    public String toString() {
        return (StringUtils.isNotBlank(payload) ? command + "> " + payload : command);
    }
}

package com.mimecast.robin.smtp.transaction;

import org.apache.commons.lang3.StringUtils;

/**
 * Transaction.
 * <p>This provides a container for SMTP transaction details.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class Transaction {

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
     *
     * @return Response code string.
     */
    public String getResponseCode() {
        return response.substring(0, 3);
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

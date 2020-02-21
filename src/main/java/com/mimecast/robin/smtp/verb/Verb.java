package com.mimecast.robin.smtp.verb;

import com.mimecast.robin.main.Config;

/**
 * SMTP verb.
 *
 * <p>This implements the basic parsing for SMTP verbs.
 *
 * @see AuthVerb
 * @see BdatVerb
 * @see EhloVerb
 * @see MailVerb
 */
public class Verb {

    /**
     * Command string.
     */
    private final String command;

    /**
     * Command parts.
     */
    final String[] parts;

    /**
     * Constructs a new Verb instance with given command.
     *
     * @param command SMTP command.
     */
    public Verb(String command) {
        this.command = command.trim();
        this.parts = command.split("(\\s+)?(\\s+|:|=)(\\s+)?");
    }

    /**
     * Constructs a new Verb instance with given Verb.
     *
     * @param verb Verb instance.
     */
    public Verb(Verb verb) {
        this.command = verb.getCommand();
        this.parts = command.split("(\\s+)?(\\s+|:|=)(\\s+)?");
    }

    /**
     * Gets SMTP command.
     *
     * @return SMTP command.
     */
    @SuppressWarnings("WeakerAccess")
    public String getCommand() {
        return command;
    }

    /**
     * Gets part count.
     *
     * @return Part count.
     */
    public int getCount() {
        return parts != null ? parts.length : 0;
    }

    /**
     * Gets SMTP command part by key.
     *
     * @param part Part key.
     * @return Part string.
     */
    public String getPart(int part) {
        return parts[part];
    }

    /**
     * Gets verb.
     *
     * @return Verb string.
     */
    public String getVerb() {
        return parts != null ? parts[0] : "";
    }

    /**
     * Gets key.
     *
     * @return Key string.
     */
    public String getKey() {
        return getVerb().toLowerCase();
    }

    /**
     * Gets parameter by name.
     *
     * @param name Parameter name string.
     * @return Value string.
     */
    public String getParam(String name) {
        // Look for it.
        for (int i = 0; i < parts.length; i++) {
            String splinter = parts[i];

            // Find the FROM keyword.
            if (splinter.equalsIgnoreCase(name)) {

                // Move to next entry.
                return parts[i + 1];
            }
        }

        return "";
    }

    /**
     * Is command error.
     * <p>Check if command is too short to be valid.
     *
     * @return Boolean.
     */
    public boolean isError() {
        return (
                // Ensures command is at least 4 chars log.
                command.length() < 4 ||

                        // Ensures AUTH is not handled if disabled.
                        (command.equalsIgnoreCase("AUTH") && !Config.getServer().isAuth()) ||

                        // Ensures STARTTLS is not handled if disabled.
                        (command.equalsIgnoreCase("STARTTLS") && !Config.getServer().isStartTls()) ||

                        // Ensures CHUNKING is not handled if disabled.
                        (command.equalsIgnoreCase("BDAT") && !Config.getServer().isChunking())
        );
    }
}

package com.mimecast.robin.smtp.verb;

import org.apache.commons.codec.binary.Base64;

/**
 * AUTH verb.
 * <p>This is used for parsing AUTH PLAIN and AUTH LOGIN commands.
 *
 * @see <a href="https://tools.ietf.org/html/rfc4954">RFC 4954</a>
 * @see <a href="https://tools.ietf.org/html/draft-murchison-sasl-login-00">DRAFT SASL LOGIN</a>
 * @see <a href="https://tools.ietf.org/html/rfc2831">RFC 2831</a>
 */
public class AuthVerb extends Verb {

    /**
     * Type container.
     * <p>PLAIN or LOGIN.
     */
    private String type = "";

    /**
     * PLAIN mechanism CID.
     */
    private String cid = "";

    /**
     * Username container.
     */
    private String username = "";

    /**
     * Password container if any.
     */
    private String password = "";

    /**
     * Constructs a new AuthVerb instance with given Verb.
     *
     * @param verb Verb instance.
     */
    public AuthVerb(Verb verb) {
        super(verb);
    }

    /**
     * Gets AUTH type.
     *
     * @return AUTH type string.
     */
    public String getType() {
        if (type.isEmpty()) {
            type = parts.length > 1 ? parts[1] : "";
        }

        return type.toUpperCase();
    }

    /**
     * Parse.
     */
    private void parse() {
        if (getType().equalsIgnoreCase("PLAIN") && parts.length > 2) {
            String decoded = new String(Base64.decodeBase64(parts[2]));
            String[] parts = decoded.split("\u0000");
            if (parts.length == 3) {
                cid = parts[0];
                username = parts[1];
                password = parts[2];
            }
        } else if (getType().equalsIgnoreCase("LOGIN") && parts.length > 2) {
            username = new String(Base64.decodeBase64(parts[2]));
        }
    }

    /**
     * Gets CID.
     *
     * @return CID string.
     */
    public String getCid() {
        if (cid.isEmpty()) {
            parse();
        }

        return cid;
    }

    /**
     * Gets username.
     *
     * @return Username string.
     */
    public String getUsername() {
        if (username.isEmpty()) {
            parse();
        }

        return username;
    }

    /**
     * Sets username.
     *
     * @param username Username string.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets password.
     *
     * @return Password string.
     */
    public String getPassword() {
        if (password.isEmpty()) {
            parse();
        }

        return password;
    }

    /**
     * Sets password.
     *
     * @param password Password string.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}

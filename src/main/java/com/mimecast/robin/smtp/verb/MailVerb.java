package com.mimecast.robin.smtp.verb;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;

/**
 * MAIL verb.
 * <p>This is used for parsing MAIL and RCPT commands.
 * @link https://tools.ietf.org/html/rfc1870 RFC 1870
 * @link https://tools.ietf.org/html/rfc3030 RFC 3030
 * @link https://tools.ietf.org/html/rfc3461 RFC 3461
 * @link https://tools.ietf.org/html/rfc6152 RFC 6152
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
class MailVerb extends Verb {

    /**
     * TO address.
     */
    private InternetAddress address;

    /**
     * SIZE parameter if any.
     */
    private int size = 0;

    /**
     * BODY parameter if any.
     */
    private String body = "";

    /**
     * RET parameter if any.
     */
    private String ret = "";

    /**
     * ENVID parameter if any.
     */
    private String envId = "";

    /**
     * NOTIFY parameter if any.
     */
    private String[] notify = new String[]{};

    /**
     * ORCPT parameter if any.
     */
    private InternetAddress oRcpt;

    /**
     * Constructs a new MailhVerb instance with given Verb.
     *
     * @param verb Verb instance.
     */
    public MailVerb(Verb verb) {
        super(verb);
    }

    /**
     * Gets MAIL address.
     *
     * @return Address.
     * @throws IOException MAIL or RCPT address parsing problem.
     */
    public InternetAddress getAddress() throws IOException {
        // From cache.
        if (address != null) {
            return address;
        }

        // If MAIL FROM.
        if (parts[0].equalsIgnoreCase("mail")) {
            try {
                address = new InternetAddress(getParam("from"));
            } catch (AddressException e) {
                throw new IOException(e);
            }
        }

        // If RCPT TO.
        if (parts[0].equalsIgnoreCase("rcpt")) {
            try {
                address = new InternetAddress(getParam("to"));
            } catch (AddressException e) {
                throw new IOException(e);
            }
        }

        return address;
    }

    /**
     * Gets MAIL SIZE.
     *
     * @return SIZE parameter.
     */
    public int getSize() {
        if (size == 0) {
            size = Integer.parseInt(getParam("size"));
        }

        return size;
    }

    /**
     * Gets MAIL BODY.
     *
     * @return BODY parameter.
     */
    public String getBody() {
        if (body.isEmpty()) {
            body = getParam("body");
        }

        return body;
    }

    /**
     * Gets RET.
     *
     * @return RET parameter.
     */
    public String getRet() {
        if (ret.isEmpty()) {
            ret = getParam("ret");
        }

        return ret;
    }

    /**
     * Gets MAIL ENVID.
     *
     * @return ENVID parameter.
     */
    public String getEnvId() {
        if (envId.isEmpty()) {
            envId = getParam("envid");
        }

        return envId;
    }

    /**
     * Get MAIL NOTIFY.
     *
     * @return NOTIFY parameter string array.
     */
    public String[] getNotify() {
        if (notify.length == 0) {
            String param = getParam("notify");
            if (!param.isEmpty()) {
                notify = param.split(",");
            }
        }

        return notify;
    }

    /**
     * Gets MAIL ORCPT.
     *
     * @return ORCPT parameter.
     * @throws IOException MAIL address parsing problem.
     */
    public InternetAddress getORcpt() throws IOException {
        if (oRcpt == null) {
            String rcpt = getParam("orcpt");
            String[] parts = rcpt.split(";");
            if (parts.length > 1 && parts[0].equalsIgnoreCase("rfc822")) {
                try {
                    oRcpt = new InternetAddress(parts[1]);
                } catch (AddressException e) {
                    throw new IOException(e);
                }
            }
        }

        return oRcpt;
    }
}

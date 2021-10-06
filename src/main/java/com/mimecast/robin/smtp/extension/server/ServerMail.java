package com.mimecast.robin.smtp.extension.server;

import com.mimecast.robin.config.server.ScenarioConfig;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.verb.Verb;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.util.Optional;

/**
 * MAIL extension processor.
 *
 * @see <a href="https://tools.ietf.org/html/rfc1870">RFC 1870</a>
 * @see <a href="https://tools.ietf.org/html/rfc3030">RFC 3030</a>
 * @see <a href="https://tools.ietf.org/html/rfc3461">RFC 3461</a>
 * @see <a href="https://tools.ietf.org/html/rfc6152">RFC 6152</a>
 */
public class ServerMail extends ServerProcessor {

    /**
     * MAIL FROM address.
     */
    protected InternetAddress address;

    /**
     * MAIL FROM SIZE param (if any).
     */
    private int size = 0;

    /**
     * MAIL FROM BODY param (if any).
     */
    private String body = "";

    /**
     * MAIL FROM RET param (if any).
     */
    private String ret = "";

    /**
     * MAIL FROM ENVID param (if any).
     */

    private String envId = "";
    /**
     * MAIL FROM NOTIFY list param (if any).
     */
    private String[] notify = new String[]{};

    /**
     * MAIL FROM ORCPT param (if any).
     */
    private InternetAddress oRcpt;

    /**
     * Advert getter.
     *
     * @return Advert string.
     */
    @Override
    public String getAdvert() {
        return "SMTPUTF8";
    }

    /**
     * MAIL processor.
     *
     * @param connection Connection instance.
     * @param verb       Verb instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection, Verb verb) throws IOException {
        super.process(connection, verb);

        // Bypass for RCPT extension.
        if (verb.getKey().equals("mail")) {

            // ScenarioConfig response.
            Optional<ScenarioConfig> opt = connection.getScenario();
            if (opt.isPresent() && opt.get().getMail() != null) {
                connection.write(opt.get().getMail());
            }

            // Accept all.
            else {
                connection.getSession().setMail(getAddress());
                connection.write("250 2.1.0 Sender OK");
            }
        }

        return true;
    }

    /**
     * Gets MAIL FROM address.
     *
     * @return Address instance.
     * @throws IOException MAIL address parsing problem.
     */
    public InternetAddress getAddress() throws IOException {
        if (address == null) {
            try {
                address = new InternetAddress(verb.getParam("from"));
            } catch (AddressException e) {
                throw new IOException(e);
            }
        }

        return address;
    }

    /**
     * Gets MAIL FROM SIZE param.
     *
     * @return Size in bytes.
     * @see <a href="https://tools.ietf.org/html/rfc1870">RFC 1870</a>
     */
    public int getSize() {
        if (size == 0) {
            size = Integer.parseInt(verb.getParam("size"));
        }

        return size;
    }

    /**
     * Gets MAIL FROM BODY param.
     *
     * @return BODY string.
     * @see <a href="https://tools.ietf.org/html/rfc3030">RFC 3030</a>
     * @see <a href="https://tools.ietf.org/html/rfc6152">RFC 6152</a>
     */
    public String getBody() {
        if (body.isEmpty()) {
            body = verb.getParam("body");
        }

        return body;
    }

    /**
     * Gets MAIL FROM NOTIFY list param.
     *
     * @return NOTIFY addresses as array list.
     * @see <a href="https://tools.ietf.org/html/rfc3461#section-4.1">RFC 3461 #4.1</a>
     */
    public String[] getNotify() {
        if (notify.length == 0) {
            String param = verb.getParam("notify");
            if (!param.isEmpty()) {
                notify = param.split(",");
            }
        }

        return notify;
    }

    /**
     * Gets MAIL FROM ORCPT param.
     *
     * @return Original recipient address string.
     * @throws IOException MAIL address parsing problem.
     * @see <a href="https://tools.ietf.org/html/rfc3461#section-4.2">RFC 3461 #4.2</a>
     */
    public InternetAddress getORcpt() throws IOException {
        if (oRcpt == null) {
            String rcpt = verb.getParam("orcpt");
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

    /**
     * Gets MAIL FROM RET param.
     *
     * @return RET string.
     * @see <a href="https://tools.ietf.org/html/rfc3461#section-4.3">RFC 3461 #4.3</a>
     */
    public String getRet() {
        if (ret.isEmpty()) {
            ret = verb.getParam("ret");
        }

        return ret;
    }

    /**
     * Gets MAIL FROM ENVID param.
     *
     * @return ENVID string.
     * @see <a href="https://tools.ietf.org/html/rfc3461#section-4.4">RFC 3461 #4.4</a>
     */
    public String getEnvId() {
        if (envId.isEmpty()) {
            envId = verb.getParam("envid");
        }

        return envId;
    }
}

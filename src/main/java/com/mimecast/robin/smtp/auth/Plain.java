package com.mimecast.robin.smtp.auth;

import com.mimecast.robin.smtp.connection.Connection;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Plain authentication mechanism.
 * @link https://tools.ietf.org/html/rfc4616 RFC4616
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class Plain {

    /**
     * Username.
     */
    private final String username;

    /**
     * Password.
     */
    private final String password;

    /**
     * Constructs a new Plain instance.
     *
     * @param connection Connection instance.
     */
    public Plain(Connection connection) {
        if (connection.getSession() != null) {
            this.username = connection.getSession().getUsername();
            this.password = connection.getSession().getPassword();
        } else {
            this.username = "";
            this.password = "";
        }
    }

    /**
     * Gets response.
     *
     * @return Response string.
     */
    public String getLogin() {
        ByteArrayOutputStream plain = new ByteArrayOutputStream();
        plain.write(username.getBytes(), 0, username.length());
        plain.write(0);
        plain.write(username.getBytes(), 0, username.length());
        plain.write(0);
        plain.write(password.getBytes(), 0, password.length());

        return Base64.encodeBase64String(plain.toByteArray());
    }
}

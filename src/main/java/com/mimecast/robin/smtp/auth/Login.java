package com.mimecast.robin.smtp.auth;

import com.mimecast.robin.smtp.connection.Connection;
import org.apache.commons.codec.binary.Base64;

/**
 * Login authentication mechanism.
 *
 * @see <a href="https://tools.ietf.org/html/draft-murchison-sasl-login-00">DRAFT SASL LOGIN</a>
 */
public class Login {

    /**
     * Username.
     */
    private final String username;

    /**
     * Password.
     */
    private final String password;

    /**
     * Constructs a new Login instance.
     *
     * @param connection Connection instance.
     */
    public Login(Connection connection) {
        if (connection.getSession() != null) {
            this.username = connection.getSession().getUsername();
            this.password = connection.getSession().getPassword();
        } else {
            this.username = "";
            this.password = "";
        }
    }

    /**
     * Gets username.
     *
     * @return Username string.
     */
    public String getUsername() {
        return Base64.encodeBase64String(username.getBytes());
    }

    /**
     * Gets password.
     *
     * @return Password string.
     */
    public String getPassword() {
        return Base64.encodeBase64String(password.getBytes());
    }
}

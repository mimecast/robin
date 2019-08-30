package com.mimecast.robin.smtp.connection;

import java.io.IOException;

/**
 * Smtp exception.
 * <p>This is thrown when there was a problem with the SMTP exchange.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class SmtpException extends IOException {

    /**
     * Constructs a new SmtpException instance.
     */
    public SmtpException() {
        super();
    }

    /**
     * Constructs a new SmtpException instance with given Exception.
     */
    public SmtpException(Exception e) {
        super(e);
    }

    /**
     * Constructs a new SmtpException instance with given message.
     *
     * @param message Message string.
     */
    public SmtpException(String message) {
        super(message);
    }
}

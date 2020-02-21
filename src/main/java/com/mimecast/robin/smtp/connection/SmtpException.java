package com.mimecast.robin.smtp.connection;

import java.io.IOException;

/**
 * Smtp exception.
 *
 * <p>This is thrown when there was a problem with the SMTP exchange.
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
     *
     * @param exception Exception instance.
     */
    public SmtpException(Exception exception) {
        super(exception);
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

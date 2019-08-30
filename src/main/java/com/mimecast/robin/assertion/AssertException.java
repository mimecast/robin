package com.mimecast.robin.assertion;

import com.mimecast.robin.assertion.mta.AssertMta;

/**
 * Assertion exception.
 * <p>Thrown by Assert and AssertMta if an assertion doesn't match anything.
 *
 * @see Assert
 * @see AssertMta
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class AssertException extends Exception {

    /**
     * Constructs a new AssertException instance without message.
     */
    public AssertException() {
        super();
    }

    /**
     * Constructs a new AssertException instance with message.
     *
     * @param message Message string.
     */
    public AssertException(String message) {
        super(message);
    }

    /**
     * Constructs a new AssertException instance with given Throwable.
     *
     * @param cause Throwable cause.
     */
    public AssertException(Throwable cause) {
        super(cause);
    }
}

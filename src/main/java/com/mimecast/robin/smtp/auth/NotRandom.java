package com.mimecast.robin.smtp.auth;

/**
 * Digest-MD5 authentication mechanism returning predefined string instead of a random one.
 * <p>This is used for verifying challenge responses.
 *
 * @see DigestMD5
 * @see Random
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class NotRandom implements Random {

    /**
     * Source container.
     */
    private final String source;

    /**
     * Constructs a new NotRandom instance with given source.
     *
     * @param source Source string.
     */
    public NotRandom(String source) {
        this.source = source;
    }

    /**
     * Returns constructor given source.
     *
     * @param size This is ignored.
     * @return Given string.
     */
    public String generate(int size) {
        return source;
    }
}

package com.mimecast.robin.smtp.auth;

import org.apache.geronimo.mail.util.Hex;

/**
 * Digest-MD5 authentication mechanism random generator.
 * <p>This is the default implementation of Random.
 *
 * @see DigestMD5
 * @see Random
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class SecureRandom implements Random {

    /**
     * Generates random bytes and HEX encodes them.
     *
     * @param size Random bytes size prior to encoding.
     * @return Random.
     */
    public String generate(int size) {
        byte[] bytes = new byte[size];
        new java.security.SecureRandom().nextBytes(bytes);

        return new String(Hex.encode(bytes));
    }
}
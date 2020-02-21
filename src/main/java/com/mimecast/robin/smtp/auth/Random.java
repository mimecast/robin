package com.mimecast.robin.smtp.auth;

/**
 * Digest-MD5 authentication mechanism random generator.
 */
interface Random {

    /**
     * Generates random bytes and HEX encodes them
     *
     * @param size Random bytes size prior to encoding.
     * @return Random.
     */
    String generate(int size);
}
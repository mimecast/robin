package com.mimecast.robin.smtp.auth;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Digest-MD5 authentication mechanism database.
 * <p>It would be wise to implement limitations in both size and time.
 * <p>While the size should be based on hardware TTL should be at most an hour.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
@SuppressWarnings("squid:S1610")
public abstract class DigestCache {

    /**
     * Saves a DigestData instance.
     *
     * @param token Lookup token string.
     * @param data  DigestData instance.
     */
    public void put(String token, DigestData data) {
        add(token, data);
    }

    /**
     * Finds a DigestData instance.
     *
     * @param token Lookup token string.
     * @return DigestData instance.
     */
    public DigestData find(String token) {
        DigestData data = new DigestData();

        if (StringUtils.isNotBlank(token)) {
            Map<String, String> search = lookup(token);
            if(search != null) {
                data.setMap(search);
            }
        }

        return data;
    }

    /**
     * Adds a DigestData instance.
     *
     * @param token Lookup token string.
     * @param data  DigestData instance.
     */
    abstract void add(String token, DigestData data);

    /**
     * Lookup DigestData in cache by token.
     *
     * @param token Token string.
     * @return DigestData instance.
     */
    abstract Map<String, String> lookup(String token);
}

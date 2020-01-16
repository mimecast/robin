package com.mimecast.robin.smtp.auth;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Digest-MD5 authentication mechanism database.
 * <p>It would be wise to implement limitations in both size and time.
 * <p>While the size should be based on hardware TTL should be at most an hour.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class StaticDigestCache extends DigestCache {

    /**
     * Static Deque cache.
     */
    private static final LinkedHashMap<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Map<String, String>> eldest) {
            return this.size() > 100; // Limit,
        }
    };

    /**
     * Adds a DigestData instance.
     *
     * @param token Lookup token string.
     * @param data  DigestData instance.
     */
    @Override
    void add(String token, DigestData data) {
        map.put(token, data.getMap());
    }

    /**
     * Lookup DigestData in cache by token.
     *
     * @param token Token string.
     * @return DigestData instance.
     */
    @Override
    Map<String, String> lookup(String token) {
        return map.get(token);
    }
}

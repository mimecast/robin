package com.mimecast.robin.smtp.auth;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Digest-MD5 authentication mechanism.
 *
 * <p>This provides common functionalities for both client and server implementations.
 * <p>It automatically constructs an instance of SecureRandom.
 * <p>This can be overridden from an extension by providing a Random instance.
 *
 * @see <a href="https://tools.ietf.org/html/rfc2831">RFC 2831</a>
 * @see Random
 * @see SecureRandom
 */
public abstract class DigestMD5 {
    static final Logger log = LogManager.getLogger(DigestMD5.class);

    /**
     * DigestData.
     */
    final DigestData digestData = new DigestData();

    /**
     * Password.
     */
    String password;

    /**
     * Digest database.
     * <p>For subsequent authentication.
     */
    DigestCache database;

    /**
     * Random number generator.
     */
    private Random random = new SecureRandom();

    /**
     * Random size.
     */
    int randomSize = 32;

    /**
     * Digester instance.
     */
    MessageDigest digest;

    /**
     * US-ASCII.
     */
    final String ascii = StandardCharsets.US_ASCII.name();

    /**
     * Constructs a new DigestMD5 instance.
     *
     * @param host     Hostname string.
     * @param username Username string.
     * @param password Password string.
     * @param realm    Realm string.
     */
    DigestMD5(String host, String username, String password, String realm) {
        if (StringUtils.isBlank(username)) throw new IllegalArgumentException("Username cannot be blank");
        if (StringUtils.isBlank(password)) throw new IllegalArgumentException("Password cannot be blank");

        this.password = password;

        this.digestData.setHost(host);
        this.digestData.setUsername(username);
        this.digestData.setRealm(realm);

        try {
            this.digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.fatal("Error getting digest algorithm MD5. This is very bad.");
        }
    }

    /**
     * Sets DigestDatabase.
     *
     * @param database DigestDatabase instance.
     */
    public void setDigestDatabase(DigestCache database) {
        this.database = database;
    }

    /**
     * Sets random generator.
     *
     * @param random Random instance.
     */
    public void setRandom(Random random) {
        this.random = random;
    }

    /**
     * Sets random size.
     *
     * @param randomSize Size integer.
     */
    public void setRandomSize(int randomSize) {
        this.randomSize = randomSize;
    }

    /**
     * Generates random bytes of given size and HEX encodes them.
     *
     * @param size Random bytes size.
     * @return HEX encoded string.
     */
    String getRandom(int size) {
        return random.generate(size);
    }
}

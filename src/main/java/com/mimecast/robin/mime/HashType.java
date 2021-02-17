package com.mimecast.robin.mime;

/**
 * Hash type container.
 */
public enum HashType {

    /**
     * SHA-256 hash type.
     */
    SHA_256("SHA-256"),

    /**
     * SHA-1 hash type.
     */
    SHA_1("SHA-1"),

    /**
     * MD5 hash type.
     */
    MD_5("MD5");

    /**
     * Key container.
     */
    private String key;

    /**
     * Constructs new hash type with given string.
     *
     * @param key String name of hash type.
     */
    HashType(final String key) {
        this.key = key;
    }

    /**
     * Gets key.
     *
     * @return String name of hash type.
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets key.
     *
     * @param key String name of hash type.
     */
    public void setKey(String key) {
        this.key = key;
    }
}

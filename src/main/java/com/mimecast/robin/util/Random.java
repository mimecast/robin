package com.mimecast.robin.util;

import java.security.SecureRandom;

/**
 * Random int and string generator.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public final class Random {

    /**
     * Protected constructor.
     */
    private Random() {
        throw new IllegalStateException("Static class");
    }

    /**
     * Random string seed.
     */
    private static final String CH = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * Random generator.
     */
    private static final java.util.Random randomGenerator = new SecureRandom();

    /**
     * Random string generator with fixed length.
     *
     * @return Random string.
     */
    public static String ch() {
        return ch(20);
    }

    /**
     * Random string generator with variable length.
     *
     * @param length Length.
     * @return Random string.
     */
    public static String ch(int length) {
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < length; i++){
            str.append(CH.charAt(no(CH.length() - 1)));
        }
        return str.toString();
    }

    /**
     * Random number generator with fixed max.
     *
     * @return Random number.
     */
    public static int no() {
        return randomGenerator.nextInt(10) + 1;
    }

    /**
     * Random number generator with variable max.
     *
     * @param length Length.
     * @return Random number.
     */
    public static int no(int length) {
        return randomGenerator.nextInt(length) + 1;
    }
}

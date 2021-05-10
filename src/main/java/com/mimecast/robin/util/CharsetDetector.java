package com.mimecast.robin.util;

import org.mozilla.universalchardet.UniversalDetector;

/**
 * Charset detector.
 */
public class CharsetDetector {

    /**
     * Protected constructor.
     */
    private CharsetDetector() {
        throw new IllegalStateException("Static class");
    }

    /**
     * Gets charset.
     *
     * @param bytes Text to guess as bytes.
     * @return Charset name.
     */
    public static String getCharset(byte[] bytes) {
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(bytes, 0, bytes.length);
        detector.dataEnd();

        String charset = detector.getDetectedCharset();
        detector.reset();

        return charset;
    }
}

package com.mimecast.robin.smtp.auth;

import org.apache.commons.codec.binary.Base64;

import javax.mail.internet.HeaderTokenizer;
import javax.mail.internet.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Digest-MD5 utils.
 * <p>Provides utility methods for processing authentication data.
 *
 * @see DigestMD5Client
 * @see DigestMD5Server
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
class DigestUtils {

    /**
     * String constants.
     */
    static final String HOST = "host";
    private static final String USERNAME = "username";
    static final String REALM = "realm";
    private static final String RESPONSE = "response";
    static final String NONCE = "nonce";
    private static final String CNONCE = "cnonce";
    static final String NC = "nc";
    static final String QOP = "qop";

    /**
     * Protected constructor.
     */
    private DigestUtils() {
        throw new IllegalStateException("Static class");
    }

    /**
     * Challenge/response parser.
     *
     * @param challenge String.
     * @return Map.
     */
    public static DigestData parsePayload(String challenge) {
        if (Base64.isBase64(challenge)) {
            challenge = decode(challenge);
        }

        Map<String, String> map = new HashMap<>();
        try {
            String str;
            HeaderTokenizer tokenizer = new HeaderTokenizer(challenge, ",", true);
            HeaderTokenizer.Token token;
            while ((token = tokenizer.next()).getType() != HeaderTokenizer.Token.EOF) {
                str = token.getValue();
                if (str.equals(",")) continue;

                if (str.endsWith("=")) {
                    map.put(str.replace("=", ""), tokenizer.next().getValue());
                } else if (str.contains("=")) {
                    String[] splinters = str.split("=");
                    map.put(splinters[0], splinters[1]);
                }
            }
        } catch (ParseException e) {
            DigestMD5.log.error("Problem parsing challenge: {}", e.getMessage());
        }

        return new DigestData().setMap(map);
    }

    /**
     * Challenge/response builder.
     *
     * @param values Map.
     * @return StringBuilder.
     */
    public static StringBuilder buildPayload(Map values) {
        StringBuilder payload = new StringBuilder();

        payload.append("username=\"").append(values.containsKey(USERNAME) ? values.get(USERNAME) : "").append("\"");
        payload.append(",realm=\"").append(values.containsKey(REALM) ? values.get(REALM) : "").append("\"");

        payload.append(",qop=").append(values.containsKey(QOP) ? values.get(QOP) : "auth");
        payload.append(",nc=").append(hexadecimal(values.containsKey(NC) ? (String) values.get(NC) : "1"));

        payload.append(",nonce=\"").append(values.containsKey(NONCE) ? values.get(NONCE) : "").append("\"");
        if(values.containsKey(CNONCE)) {
            payload.append(",cnonce=\"").append(values.containsKey(CNONCE) ? values.get(CNONCE) : "").append("\"");
        }

        if(values.containsKey(HOST)) {
            payload.append(",digest-uri=\"smtp/").append(values.containsKey(HOST) ? values.get(HOST) : "").append("\"");
        }

        if(values.containsKey(RESPONSE)) {
            payload.append(",response=").append(values.containsKey(RESPONSE) ? values.get(RESPONSE) : "");
        }

        return payload;
    }

    /**
     * Converts string numeral to hex string with padding.
     *
     * @param val String value.
     * @return Hexadecimal value.
     */
    public static String hexadecimal(String val) {
        return String.format("%1$08X", Integer.parseInt(val));
    }

    /**
     * Base64 encode.
     *
     * @param decoded String to encode.
     * @return Encoded string.
     */
    public static String encode(String decoded) {
        return new String(Base64.encodeBase64(decoded.getBytes()));
    }

    /**
     * Base64 decode.
     *
     * @param encoded Encoded string.
     * @return Decoded string.
     */
    public static String decode(String encoded) {
        return new String(Base64.decodeBase64(encoded.getBytes()));
    }
}
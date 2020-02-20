package com.mimecast.robin.smtp.auth;

import org.apache.commons.lang3.StringUtils;
import org.apache.geronimo.mail.util.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Digest-MD5 authentication client implementation.
 * @link https://tools.ietf.org/html/rfc2831 RFC 2831
 *
 * @see DigestMD5
 * @see Random
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
@SuppressWarnings("squid:S1192")
public class DigestMD5Client extends DigestMD5 {
    private static final Logger log = LogManager.getLogger(DigestMD5Client.class);

    /**
     * Client head.
     */
    private String clientHead;

    /**
     * Client body.
     */
    private String clientBody;

    /**
     * Constructs a new DigestMD5Client instance.
     *
     * @param host     Hostname string.
     * @param username Username string.
     * @param password Password string.
     * @param realm    Realm string.
     */
    public DigestMD5Client(String host, String username, String password, String realm) {
        super(host, username, password, realm);
    }

    /**
     * Authenticate client.
     *
     * @param challenge String challenge to respond too.
     * @return Payload.
     */
    public String authenticateClient(String challenge) {
        DigestData data = DigestUtils.parsePayload(challenge);
        digestData.addMap(data.getMap());

        digestData.setNc("1");
        digestData.setQop("auth");
        digestData.setCnonce(getRandom(randomSize));

        digestData.setResponse(buildResponse(digestData.getMap(), digestData.getCnonce()));

        // Save DigestData for subsequent authentication.
        String token = buildToken();
        if (database != null && StringUtils.isNotBlank(token)) {
            database.put(token, digestData);
            log.debug("Response saved.");
        }

        return DigestUtils.encode(DigestUtils.buildPayload(digestData.getMap()).toString());
    }

    /**
     * Build token.
     *
     * @return Token string.
     */
    private String buildToken() {
        try {
            digest.reset();
            digest.update(digestData.getHost().getBytes(ascii));
            digest.update(digestData.getUsername().getBytes(ascii));
            // Realm not used as the server may not use any.

            String token = new String(Hex.encode(digest.digest()), ascii);
            log.debug("Token built: {}", token);

            return token;
        } catch (UnsupportedEncodingException e) {
            log.fatal("Error encoding to {}: {}", ascii, e.getMessage());
        }

        return "";
    }

    /**
     * Gets previous response if database given and entry found.
     *
     * @return DigestData instance.
     */
    private DigestData getPreviousResponse() {
        if (database != null) {
            DigestData digestData = database.find(buildToken());
            if (StringUtils.isNotBlank(digestData.getResponse())) {
                return digestData;
            }
        }

        return null;
    }

    /**
     * Gets subsequent authentication response.
     *
     * @return Payload.
     */
    public String getSubsequentAuthentication() {
        DigestData digestData = getPreviousResponse();
        if (digestData != null && StringUtils.isNotBlank(digestData.getResponse())) {
            digestData.bumpNc();

            String response = DigestUtils.encode(DigestUtils.buildPayload(digestData.getMap()).toString());
            log.debug("Found previous response: {}", response);

            // Save DigestData for subsequent authentication.
            String token = buildToken();
            if (database != null && StringUtils.isNotBlank(token)) {
                database.put(token, digestData);
                log.debug("Response updates.");
            }

            return response;
        }

        return "";
    }

    /**
     * Build response.
     *
     * @param map    Map of key value pairs parsed from challenge.
     * @param cnonce String of random cnonce for response.
     * @return Response string.
     */
    private String buildResponse(Map<String, String> map, String cnonce) {
        String response = "";
        try {
            log.debug("Building response.");
            digest.reset();

            // ID = Username:realm:password identity string.
            String id = digestData.getUsername() + ":" + map.get(DigestUtils.REALM) + ":" + password;
            log.debug("Building id: {}", id);
            digest.update(digest.digest(id.getBytes(ascii)));

            // Noneces = nonce:cnonce.
            String nonces = ":" + map.get(DigestUtils.NONCE) + ":" + cnonce;
            log.debug("Building nonces: {}", nonces);
            digest.update(nonces.getBytes(ascii));

            // Client head and body = HEX(digest):nonce:nc:cnonce:qop:HEX(digest).
            clientHead = new String(Hex.encode(digest.digest()), ascii);
            clientBody = ":" + map.get(DigestUtils.NONCE) + ":" + DigestUtils.hexadecimal(map.get(DigestUtils.NC)) + ":" + cnonce + ":" + map.get(DigestUtils.QOP) + ":";
            log.debug("Building clientHead: {}", clientHead);
            log.debug("Building clientBody: {}", clientBody);

            // Auth = AUTHENTICATE:smtp/host.
            String auth = "AUTHENTICATE:smtp/" + map.get(DigestUtils.HOST);
            digest.update(auth.getBytes(ascii));

            // Client and client tail.
            String clientTail = new String(Hex.encode(digest.digest()), ascii);
            log.debug("Building clientTail: {}", clientTail);
            String client = clientHead + clientBody + clientTail;
            log.debug("Building client: {}", client);
            digest.update(client.getBytes(ascii));

            // Return.
            response = new String(Hex.encode(digest.digest()), ascii);
            log.debug("Response built: {}", response);
        } catch (UnsupportedEncodingException e) {
            log.fatal("Error encoding to {}: {}", ascii, e.getMessage());
        }

        return response;
    }

    /**
     * Authenticate Server.
     *
     * @param response String server challenge response to validate.
     * @return True if authenticated.
     */
    public boolean authenticateServer(String response) {
        return computerServerResponse().equals(
                DigestUtils.parsePayload(response).getRspAuth()
        );
    }

    /**
     * Compute server response.
     *
     * @return Server response string.
     */
    public String computerServerResponse() {
        try {
            // Host.
            digest.reset();
            digest.update((":smtp/" + digestData.getHost()).getBytes(ascii));

            // Client.
            String client = clientHead  + clientBody + new String(Hex.encode(digest.digest()), ascii);
            log.debug("Building client: {}", client);
            digest.update(client.getBytes(ascii));

            // Return.
            return new String(Hex.encode(digest.digest()), ascii);
        } catch (UnsupportedEncodingException e) {
            log.fatal("Error encoding to {}: {}", ascii, e.getMessage());
        }
        return "";
    }
}

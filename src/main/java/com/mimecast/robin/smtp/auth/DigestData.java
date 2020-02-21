package com.mimecast.robin.smtp.auth;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Digest data.
 *
 * <p>This is a container for challenges and responses.
 *
 * @see <a href="https://tools.ietf.org/html/rfc2831#section-2.1.1">RFC 2831 #2.1.1</a>
 * @see <a href="https://tools.ietf.org/html/rfc2831#section-2.1.2">RFC 2831 #2.1.2</a>
 */
@SuppressWarnings("UnusedReturnValue")
public class DigestData {

    /**
     * Data storage map.
     */
    private Map<String, String> map = new HashMap<>();

    /**
     * Gets host.
     * <p>The DNS host name or IP address for the service requested. The
     * DNS host name must be the fully-qualified canonical name of the
     * host. The DNS host name is the preferred form...
     *
     * @return Host string.
     */
    public String getHost() {
        return getNotNull("host");
    }

    /**
     * Sets host.
     *
     * @param host Host string.
     * @return Self.
     */
    public DigestData setHost(String host) {
        setNotNull("host", host);
        return this;
    }

    /**
     * Gets username.
     * <p>The user's name in the specified realm, encoded according to the
     * value of the "charset" directive.
     *
     * @return Username string.
     */
    public String getUsername() {
        return getNotNull("username");
    }

    /**
     * Sets username.
     *
     * @param username Username string.
     * @return Self.
     */
    public DigestData setUsername(String username) {
        setNotNull("username", username);
        return this;
    }

    /**
     * Gets realm.
     * <p>The realm containing the user's account. This directive is
     * required if the server provided any realms in the
     * "digest-challenge", in which case it may appear exactly once and
     * its value SHOULD be one of those realms. If the directive is
     * missing, "realm-value" will set to the empty string...
     *
     * @return Realm string.
     */
    public String getRealm() {
        return getNotNull("realm");
    }

    /**
     * Sets realm.
     *
     * @param realm Realm string.
     * @return Self.
     */
    public DigestData setRealm(String realm) {
        setNotNull("realm", realm);
        return this;
    }

    /**
     * Gets nonce.
     * <p>The server-specified data string received in the preceding
     * digest-challenge. This directive is required and MUST be present
     * exactly once; otherwise, authentication fails.
     *
     * @return Nonce string.
     */
    public String getNonce() {
        return getNotNull("nonce");
    }

    /**
     * Sets nonce.
     *
     * @param nonce Nonce string.
     * @return Self.
     */
    public DigestData setNonce(String nonce) {
        setNotNull("nonce", nonce);
        return this;
    }

    /**
     * Gets cnonce.
     * <p>A client-specified data string which MUST be different each time a
     * digest-response is sent as part of initial authentication. The
     * cnonce-value is an opaque quoted string value provided by the
     * client and used by both client and server to avoid chosen
     * plaintext attacks, and to provide mutual authentication.
     *
     * @return CNonce string.
     */
    public String getCnonce() {
        return getNotNull("cnonce");
    }

    /**
     * Sets cnonce.
     *
     * @param cnonce CNonce string.
     * @return Self.
     */
    public DigestData setCnonce(String cnonce) {
        setNotNull("cnonce", cnonce);
        return this;
    }

    /**
     * Gets nonce-count.
     * <p>The nc-value is the hexadecimal count of the number of requests
     * (including the current request) that the client has sent with the
     * nonce value in this request.
     *
     * @return Nc string.
     */
    public String getNc() {
        return getNotNull("nc");
    }

    /**
     * Sets nonce-count.
     *
     * @param nc Nc string.
     * @return Self.
     */
    public DigestData setNc(String nc) {
        setNotNull("nc", nc);
        return this;
    }

    /**
     * Bump nonce-count.
     *
     * @return Self.
     */
    public DigestData bumpNc() {
        String ncs = StringUtils.isNotBlank(getNc()) ? getNc() : "";
        int nci = Integer.parseInt(ncs) + 1;
        map.put("nc", String.valueOf(nci));
        return this;
    }

    /**
     * Gets quality of protection.
     * <p>Indicates what "quality of protection" the client accepted. If
     * present, it may appear exactly once and  its value MUST be one of
     * the alternatives in qop-options. If not present, it defaults to
     * "auth". These values affect the computation of the response. Note
     * that this is a single token, not a quoted list of alternatives.
     *
     * @return Qop string.
     */
    public String getQop() {
        return getNotNull("qop");
    }

    /**
     * Sets quality of protection.
     *
     * @param qop Qop string.
     * @return Self.
     */
    public DigestData setQop(String qop) {
        setNotNull("qop", qop);
        return this;
    }

    /**
     * Gets response.
     * <p>A string of 32 hex digits computed as defined below, which proves
     * that the user knows a password. This directive is required and
     * MUST be present exactly once; otherwise, authentication fails.
     *
     * @return Response string.
     */
    public String getResponse() {
        return getNotNull("response");
    }

    /**
     * Sets response.
     *
     * @param response Response string.
     * @return Self.
     */
    public DigestData setResponse(String response) {
        setNotNull("response", response);
        return this;
    }

    /**
     * Gets response value.
     * <p>The server response which proves the server knows the password too.
     *
     * @return Rspauth string.
     */
    public String getRspAuth() {
        return getNotNull("rspauth");
    }

    /**
     * Sets rspauth.
     *
     * @param rspauth Rspauth string.
     * @return Self.
     */
    public DigestData setRspAuth(String rspauth) {
        setNotNull("rspauth", rspauth);
        return this;
    }

    /**
     * Gets data map.
     *
     * @return Map instance.
     */
    public Map<String, String> getMap() {
        return map;
    }

    /**
     * Sets data map.
     *
     * @param map Map instance.
     * @return Self.
     */
    public DigestData setMap(Map<String, String> map) {
        this.map = map;
        return this;
    }

    /**
     * Adds data map entries.
     *
     * @param map Map instance.
     * @return Self.
     */
    public DigestData addMap(Map<String, String> map) {
        this.map.putAll(map);
        return this;
    }

    /**
     * Sets not null map entry.
     *
     * @param key   Map key.
     * @param value Map value.
     */
    private void setNotNull(String key, String value) {
        map.put(key, StringUtils.isNotBlank(value) ? value : "");
    }

    /**
     * Gets not null map entry.
     *
     * @param key Map key.
     * @return String.
     */
    private String getNotNull(String key) {
        return StringUtils.isNotBlank(map.get(key)) ? map.get(key) : "";
    }
}

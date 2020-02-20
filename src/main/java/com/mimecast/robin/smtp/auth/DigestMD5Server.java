package com.mimecast.robin.smtp.auth;

import java.util.Optional;

/**
 * Digest-MD5 authentication server implementation.
 * @link https://tools.ietf.org/html/rfc2831 RFC 2831
 *
 * @see DigestMD5
 * @see Random
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
class DigestMD5Server extends DigestMD5 {

    /**
     * Challenge string.
     */
    private String challenge;

    /**
     * Constructs a new DigestMD5Server instance.
     *
     * @param host     Hostname string.
     * @param username Username string.
     * @param password Password string.
     * @param realm    Realm string.
     */
    public DigestMD5Server(String host, String username, String password, String realm) {
        super(host, username, password, realm);
    }

    /**
     * Generate challenge for client to solve.
     *
     * @return Challenge.
     */
    public String generateChallenge() {
        digestData.setNonce(getRandom(randomSize));
        digestData.setNc("1");
        digestData.setQop("auth");

        // Save DigestData for subsequent authentication.
        if (database != null) {
            database.put(digestData.getNonce(), digestData);
            log.debug("Challenge saved.");
        }

        challenge = DigestUtils.encode(DigestUtils.buildPayload(digestData.getMap()).toString());
        return challenge;
    }

    /**
     * Validates client response to challenge.
     *
     * @param clientResponse Client challenge response to validate.
     * @return Boolean.
     */
    public Optional<String> authenticateClient(String clientResponse) {
        DigestData clientMap = DigestUtils.parsePayload(clientResponse);
        if (!clientMap.getResponse().isEmpty() && !clientMap.getCnonce().isEmpty()) {

            // Compute our own response.
            Random notRandom = new NotRandom(clientMap.getCnonce());
            DigestMD5Client client = new DigestMD5Client(digestData.getHost(), digestData.getUsername(), password, digestData.getRealm());
            client.setRandom(notRandom);

            String ownResponse = client.authenticateClient(challenge);
            DigestData ownMap = DigestUtils.parsePayload(ownResponse);

            // Check if responses match.
            if (
                    !ownMap.getResponse().isEmpty() &&
                    !clientMap.getResponse().isEmpty() &&
                    clientMap.getResponse().equals(ownMap.getResponse())
                    ) {
                return Optional.of(DigestUtils.encode("rspauth=" + client.computerServerResponse()));
            }
        }

        return Optional.empty();
    }
}

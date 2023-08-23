package com.mimecast.robin.assertion.client.request;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.config.client.RequestConfig;
import com.mimecast.robin.main.RequestBase;
import com.mimecast.robin.smtp.session.Session;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * HTTP/S request client.
 */
public class RequestClient extends RequestBase {

    /**
     * Constructs a new RequestClient instance with given Session instance.
     *
     * @param session Session instance.
     */
    public RequestClient(Session session) {
        super(session);
    }

    /**
     * Make request with given RequestConfig instance.
     *
     * @param requestConfig RequestConfig instance.
     * @throws AssertException Assertion exception.
     * @throws IOException     Unable to communicate.
     */
    public void request(RequestConfig requestConfig) throws AssertException, IOException {
        try {
            makeRequest(requestConfig);

            // TODO Add assertion support.

        } catch (GeneralSecurityException | IOException e) {
            log.error("Connection failure: {}", e.getMessage());
        }
    }
}

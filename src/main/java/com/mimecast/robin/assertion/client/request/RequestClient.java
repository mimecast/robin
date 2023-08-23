package com.mimecast.robin.assertion.client.request;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.config.client.RequestConfig;
import com.mimecast.robin.http.HttpResponse;
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
     * @param config RequestConfig instance.
     * @throws AssertException Assertion exception.
     * @throws IOException     Unable to communicate.
     */
    public HttpResponse request(RequestConfig config) throws AssertException, IOException {
        HttpResponse httpResponse = null;
        try {
            httpResponse = makeRequest(config);

        } catch (GeneralSecurityException | IOException e) {
            log.error("Request Client: Request failure: {}", e.getMessage());
        }

        return httpResponse;
    }
}

package com.mimecast.robin.main;

import com.mimecast.robin.assertion.Assert;
import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.config.client.RequestConfig;
import com.mimecast.robin.http.HttpResponse;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.session.Session;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

/**
 * HTTP/S request client.
 */
public class RequestClient extends RequestBase {

    /**
     * Constructs a new RequestClient instance.
     */
    public RequestClient() {
        super();
    }

    /**
     * Constructs a new RequestClient instance with given Session instance.
     *
     * @param session Session instance.
     */
    public RequestClient(Session session) {
        super(session);
    }

    /**
     * Constructs a new RequestClient instance with given client configuration path.
     *
     * @param configDirPath Directory path.
     * @throws ConfigurationException Unable to read/parse config file.
     */
    public RequestClient(String configDirPath) throws ConfigurationException {
        super(configDirPath);
    }

    /**
     * Make request with given configuration path.
     *
     * @param casePath Case config path.
     * @throws AssertException Assertion exception.
     * @throws IOException     Unable to communicate.
     */
    public void request(String casePath) throws AssertException, IOException {
        RequestConfig requestConfig = getConfig(casePath);

        try {
            HttpResponse response = makeRequest(requestConfig);

            // Session.
            response.getHeaders().forEach(session::putMagic); // Add headers in magic.

            // TODO Save content in magic?

            // Assert.
            if (response.isSuccessfull()) {
                new Assert(new Connection(session).setServer(getUrlHost(requestConfig.getUrl())))
                        .run();
            } else {
                throw new AssertException("Unsuccessful request");
            }

        } catch (GeneralSecurityException | IOException e) {
            log.error("Connection failure: {}", e.getMessage());
        }
    }

    protected String getUrlHost(String url) {
        try {
            return new URI(url).getHost();
        } catch (URISyntaxException e) {
            log.error("Unable to parse URI: {}", e.getMessage());
        }

        return "";
    }
}

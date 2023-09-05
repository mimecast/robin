package com.mimecast.robin.main;

import com.google.gson.Gson;
import com.mimecast.robin.assertion.Assert;
import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.config.client.RequestConfig;
import com.mimecast.robin.http.HttpResponse;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.session.Session;
import com.mimecast.robin.util.MapUtils;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    @SuppressWarnings("unchecked")
    public void request(String casePath) throws AssertException, IOException {
        RequestConfig requestConfig = getConfig(casePath);

        HttpResponse httpResponse = null;
        try {
            httpResponse = makeRequest(requestConfig);

            // Session.
            httpResponse.getHeaders().forEach(session::putMagic); // Add headers in magic.

            // Save results.
            List<String> response = new ArrayList<>();
            String responseCT = httpResponse.getHeaders().get("Content-Type");

            if (responseCT != null && responseCT.toLowerCase().contains("/json")) {
                MapUtils.flattenMap(new Gson().fromJson(httpResponse.getBody(), Map.class), "", response);
            } else {
                response.addAll(List.of(httpResponse.getBody().split("\n")));
            }

            session.saveResults("response", response);

        } catch (GeneralSecurityException | IOException e) {
            log.error("Request Client: Request failure: {}", e.getMessage());
        }

        // Assert.
        if (httpResponse != null && httpResponse.isSuccessfull()) {
            Connection connection = new Connection(session)
                    .setServer(getUrlHost(requestConfig.getUrl()));
            connection.getSession().getSessionTransactionList().addTransaction("HTTP", new Gson().toJson(httpResponse.getHeaders()), !httpResponse.isSuccessfull());

            new Assert(connection).run();

        } else {
            throw new AssertException("Request Client: Unable to make request");
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

package com.mimecast.robin.main;

import com.mimecast.robin.assertion.Assert;
import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.config.client.CaseConfig;
import com.mimecast.robin.config.client.RequestConfig;
import com.mimecast.robin.http.HttpClient;
import com.mimecast.robin.http.HttpMethod;
import com.mimecast.robin.http.HttpRequest;
import com.mimecast.robin.http.HttpResponse;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.session.Session;

import javax.mail.internet.InternetHeaders;
import javax.naming.ConfigurationException;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * HTTP/S request client.
 */
public final class RequestClient extends Foundation {

    /**
     * Supported request types.
     */
    enum RequestType {
        DELETE,
        POST,
        PUT,
        GET
    }

    /**
     * Permissive trust manager.
     */
    private final X509TrustManager trustManager = Factories.getTrustManager();

    /**
     * Confing instance.
     */
    private final BasicConfig config;

    /**
     * Constructs a new RequestClient instance with given client configuration path.
     * <p>To be used in combination with the Junit launcher service.
     */
    public RequestClient() {
        config = new BasicConfig(Config.getProperties().getMapProperty("request"));
    }

    /**
     * Constructs a new RequestClient instance with given client configuration path.
     *
     * @param configDirPath Directory path.
     * @throws ConfigurationException Unable to read/parse config file.
     */
    public RequestClient(String configDirPath) throws ConfigurationException {
        init(configDirPath);
        config = new BasicConfig(Config.getProperties().getMapProperty("request"));
    }


    /**
     * Make request with configuration path.
     *
     * @param casePath Case config path.
     * @throws AssertException Assertion exception.
     * @throws IOException     Unable to communicate.
     */
    public void request(String casePath) throws AssertException, IOException {
        CaseConfig caseConfig = new CaseConfig(casePath);
        RequestConfig requestConfig = new RequestConfig(caseConfig.getMapProperty("request"));

        try {
            // Selecting the HTTP Request method.
            HttpMethod method;
            switch (requestConfig.getType()) {
                case "DELETE":
                    method = HttpMethod.DELETE;
                    break;

                case "POST":
                    method = HttpMethod.POST;
                    break;

                case "PUT":
                    method = HttpMethod.PUT;
                    break;

                default:
                    method = HttpMethod.GET;
                    break;

            }

            // Build request.
            HttpRequest request = new HttpRequest(requestConfig.getUrl(), method);

            // Add headers.
            InternetHeaders headers = requestConfig.getHeaders();
            if (headers.getHeader("Cache-Control") == null) {
                request.addHeader("Cache-Control", "no-cache");
            }
            Collections.list(headers.getAllHeaders()).forEach(h -> request.addHeader(h.getName(), h.getValue()));

            // Add params.
            requestConfig.getParams().forEach(request::addParam);

            // Add files.
            if (!requestConfig.getFiles().isEmpty()) {
                requestConfig.getFiles().forEach((key, value) -> request.addFile(key, value, "application/octet-stream"));
            }

            // Add content.
            if (requestConfig.getContent() != null) {
                request.addContent(requestConfig.getContent().getKey(), requestConfig.getContent().getValue());
            }

            // Request.
            log.info("Request Client Request: {}", request);
            HttpResponse response = new HttpClient(config, trustManager).execute(request);
            log.info("Request Client Headers: {}", response.getHeaders());
            log.info("Request Client Response: {}", response.getBody());

            // Session.
            Session session = Factories.getSession();
            response.getHeaders().forEach(session::putMagic); // Add headers as magic

            // TODO Extend this to add magic from response body accordingly.

            // Assert.
            new Assert(new Connection(session)).run();

        } catch (GeneralSecurityException | IOException e) {
            log.error("Connection failure: {}", e.getMessage());
        }
    }
}

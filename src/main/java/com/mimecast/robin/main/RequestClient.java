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
import java.net.URI;
import java.net.URISyntaxException;
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
     * Session instance.
     */
    private final Session session;

    /**
     * Constructs a new RequestClient instance with given client configuration path.
     * <p>To be used in combination with the Junit launcher service.
     */
    public RequestClient() {
        config = new BasicConfig(Config.getProperties().getMapProperty("request"));
        session = Factories.getSession();
    }

    /**
     * Constructs a new RequestClient instance with given Session instance.
     *
     * @param session Session instance.
     */
    public RequestClient(Session session) {
        config = new BasicConfig(Config.getProperties().getMapProperty("request"));
        this.session = session;
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
        session = Factories.getSession();
    }


    /**
     * Make request with configuration path.
     *
     * @param casePath Case config path.
     * @throws AssertException Assertion exception.
     * @throws IOException     Unable to communicate.
     */
    public void request(String casePath) throws AssertException, IOException {
        RequestConfig requestConfig = getConfig(casePath);

        try {
            HttpRequest request = getRequest(casePath, requestConfig);
            log.info("Request Client Request: {}", request);
            HttpResponse response = new HttpClient(config, trustManager).execute(request);
            log.info("Request Client Headers: {}", response.getHeaders());
            log.info("Request Client Response: {}", response.getBody());

            // Session.
            response.getHeaders().forEach(session::putMagic); // Add headers as magic

            // TODO Extend this to add magic from response body accordingly.

            // Assert.
            if (response.isSuccessfull()) {
                new Assert(new Connection(session).setServer(getUrlHost(requestConfig.getUrl()))).run();
            } else {
                throw new AssertException("Unsuccessful request");
            }

        } catch (GeneralSecurityException | IOException e) {
            log.error("Connection failure: {}", e.getMessage());
        }
    }

    /**
     * Gets RequestConfig instance.
     *
     * @param casePath Case config path.
     * @return RequestConfig instance.
     * @throws IOException Unable to communicate.
     */
    private RequestConfig getConfig(String casePath) throws IOException {
        CaseConfig caseConfig = new CaseConfig(casePath);
        session.map(caseConfig);
        return new RequestConfig(caseConfig.getMapProperty("request"), session);
    }

    /**
     * Gets HttpRequest instance.
     *
     * @param casePath      Case config path.
     * @param requestConfig RequestConfig instance.
     * @return HttpRequest instance.
     */
    private HttpRequest getRequest(String casePath, RequestConfig requestConfig) {
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

        // Add object.
        if (requestConfig.getObject() != null) {
            request.addObject(requestConfig.getObject().getKey(), requestConfig.getObject().getValue());
        }

        return request;
    }

    private String getUrlHost(String url) {
        try {
            return new URI(url).getHost();
        } catch (URISyntaxException e) {
            log.error("Unable to parse URI: {}", e.getMessage());
        }

        return "";
    }

    public Session getSession() {
        return session;
    }
}

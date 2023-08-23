package com.mimecast.robin.main;

import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.config.client.CaseConfig;
import com.mimecast.robin.config.client.RequestConfig;
import com.mimecast.robin.http.HttpClient;
import com.mimecast.robin.http.HttpMethod;
import com.mimecast.robin.http.HttpRequest;
import com.mimecast.robin.http.HttpResponse;
import com.mimecast.robin.smtp.session.Session;

import javax.mail.internet.InternetHeaders;
import javax.naming.ConfigurationException;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * HTTP/S request base.
 */
public abstract class RequestBase extends Foundation {

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
    protected final X509TrustManager trustManager = Factories.getTrustManager();

    /**
     * Confing instance.
     */
    protected final BasicConfig config;

    /**
     * Session instance.
     */
    protected final Session session;

    /**
     * Constructs a new RequestBase instance with given client configuration path.
     * <p>To be used in combination with the Junit launcher service.
     */
    public RequestBase() {
        config = new BasicConfig(Config.getProperties().getMapProperty("request"));
        session = Factories.getSession();
    }

    /**
     * Constructs a new RequestBase instance with given Session instance.
     *
     * @param session Session instance.
     */
    public RequestBase(Session session) {
        config = new BasicConfig(Config.getProperties().getMapProperty("request"));
        this.session = session;
    }

    /**
     * Constructs a new RequestBase instance with given client configuration path.
     *
     * @param configDirPath Directory path.
     * @throws ConfigurationException Unable to read/parse config file.
     */
    public RequestBase(String configDirPath) throws ConfigurationException {
        init(configDirPath);
        config = new BasicConfig(Config.getProperties().getMapProperty("request"));
        session = Factories.getSession();
    }

    /**
     * Gets Session instance.
     *
     * @return Session instance.
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets RequestConfig instance.
     *
     * @param casePath Case config path.
     * @return RequestConfig instance.
     * @throws IOException Unable to communicate.
     */
    protected RequestConfig getConfig(String casePath) throws IOException {
        CaseConfig caseConfig = new CaseConfig(casePath);
        session.map(caseConfig);
        return new RequestConfig(caseConfig.getMapProperty("request"), session);
    }

    /**
     * Gets HttpRequest instance.
     *
     * @param requestConfig RequestConfig instance.
     * @return HttpRequest instance.
     */
    protected HttpRequest getRequest(RequestConfig requestConfig) {
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

    /**
     * Make request with given RequestConfig instance.
     *
     * @param requestConfig RequestConfig instance.
     * @return HttpResponse instance.
     * @throws IOException              Unable to communicate.
     * @throws GeneralSecurityException Unable to negociate TLS.
     */
    protected HttpResponse makeRequest(RequestConfig requestConfig) throws IOException, GeneralSecurityException {
        HttpRequest request = getRequest(requestConfig);
        log.info("Request Client Request: {}", request);

        HttpResponse response = new HttpClient(config, trustManager)
                .execute(request);
        log.info("Request Client Headers: {}", response.getHeaders());
        log.info("Request Client Response: {}", response.getBody());

        return response;
    }
}

package com.mimecast.robin.assertion.client.request;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.assertion.client.ExternalClient;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.config.client.RequestConfig;

import java.io.IOException;

/**
 * Request external client.
 *
 * <p>This is a basic HTTP request client.</p>
 * <p>Example configuration:</p>
 * <pre>
 * {
 *   "type": "request",
 *   "wait": 5,
 *   "delay": 5,
 *   "retry": 3,
 *   request: {
 *     url: "https://robin.requestcatcher.com/",
 *     type: "POST",
 *     headers: [
 *       {
 *         name: "Content-Type",
 *         value: "application/json"
 *       },
 *       {
 *         name: "Cache-Control",
 *         value: "no-cache"
 *       },
 *       {
 *         name: "Authorization",
 *         value: "Basic {$requestAuth}"
 *       }
 *     ],
 *     content: {
 *       path: "src/test/resources/cases/config/request/example.json5",
 *       mimeType: "application/json"
 *     }
 *   }
 * },
 * </pre>
 */
public class RequestExternalClient extends ExternalClient {

    /**
     * Assert external config instance.
     */
    protected BasicConfig config;

    /**
     * Sets config instance.
     *
     * @param config ExternalClient instance.
     * @return Self.
     */
    @Override
    public ExternalClient setConfig(BasicConfig config) {
        super.setConfig(config);
        this.config = config;
        return this;
    }

    @Override
    public void run() throws AssertException {
        try {
            new RequestClient(connection.getSession())
                    .request(new RequestConfig(config.getMapProperty("request"), connection.getSession()));
        } catch (IOException e) {
            throw new AssertException(e.getMessage());
        }
    }
}

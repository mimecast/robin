package com.mimecast.robin.assertion.client.request;

import com.google.gson.Gson;
import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.assertion.client.MatchExternalClient;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.config.assertion.external.MatchExternalClientConfig;
import com.mimecast.robin.config.client.RequestConfig;
import com.mimecast.robin.http.HttpResponse;
import com.mimecast.robin.util.MapUtils;
import com.mimecast.robin.util.Sleep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class RequestExternalClient extends MatchExternalClient {

    /**
     * Assert external config instance.
     */
    protected MatchExternalClientConfig config;

    /**
     * Sets config instance.
     *
     * @param config ExternalClient instance.
     * @return Self.
     */
    @Override
    public RequestExternalClient setConfig(BasicConfig config) {
        super.setConfig(config);
        this.config = new MatchExternalClientConfig(config.getMap());
        return this;
    }

    /**
     * Run assertions.
     *
     * @throws AssertException Assertion exception.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void run() throws AssertException {
        List<String> data = new ArrayList<>();
        HttpResponse httpResponse = makeRequest();

        // Path based data collection for JSON format.
        String responseCT = httpResponse.getHeaders().get("Content-Type");
        if (responseCT != null && responseCT.toLowerCase().contains("/json")) {

            // Parse JSON.
            Map<String, Object> map = new Gson().fromJson(httpResponse.getBody(), Map.class);
            if (map != null && !map.isEmpty()) {

                // Flatten map.
                MapUtils.flattenMap(map, "", data);

                // Filter data.
                List<String> paths = config.getListProperty("paths");
                data = data.stream().filter(s -> {
                    for (String path : paths) {
                        if (s.startsWith(path)) {
                            return true;
                        }
                    }
                    return false;
                }).collect(Collectors.toList());
            }
        }

        // Treat data as text and split into lines if any.
        else {
            data.addAll(List.of(httpResponse.getBody().split("\n")));
        }

        // Verify no data was found.
        if (data.isEmpty()) {
            if (verifyNone) {
                log.info("AssertExternal data verify none");
                logResults(data);

            } else {
                throw new AssertException("No request data found to assert against");
            }
        }
        // Verify found data.
        else {
            // Precompile verify patterns for performance.
            compileVerify();

            if (checkVerify(data)) {
                log.debug("AssertExternal request response verify success");
                logResults(data);

                runMatches(data);
            }

            // Skip fail on verify.
            else if (!assertVerifyFails) {
                skip = true;
                log.warn("Skipping");
            }
        }
    }

    /**
     * Makes HTTP request
     *
     * @return HttpResponse instance.
     * @throws AssertException Assertion exception.
     */
    protected HttpResponse makeRequest() throws AssertException {
        HttpResponse httpResponse = null;

        long delay = config.getWait() > 0 ? config.getWait() * 1000L : 0L;
        for (int count = 0; count < config.getRetry(); count++) {
            Sleep.nap((int) delay);
            log.info("AssertExternal request attempt {} of {}", count + 1, config.getRetry());

            try {
                RequestConfig requestConfig = new RequestConfig(config.getMapProperty("request"), connection.getSession());
                httpResponse = new RequestClient(connection.getSession())
                        .request(requestConfig);

                // Retry delay if needed.
                delay = config.getDelay() * 1000L;

                // Verify response.
                if (httpResponse == null || !httpResponse.isSuccessfull()) {
                    log.info("AssertExternal request verify {}", (count < config.getRetry() - 1 ? "failure" : "attempts spent"));
                    continue;
                }

                // Verify response Content-Type is as requested if any requested.
                String[] configCT = requestConfig.getHeaders().getHeader("Content-Type");
                String responseCT = httpResponse.getHeaders().get("Content-Type");

                // If we requested a Content-Type ensure it matches.
                if (configCT.length > 0 && (responseCT == null || !responseCT.toLowerCase().startsWith(configCT[0].toLowerCase()))) {
                    log.info("AssertExternal request Content-Type did not verify: {} != {}", configCT[0], responseCT);
                    continue;
                }

                break;

            } catch (IOException e) {
                throw new AssertException(e.getMessage());
            }
        }

        return httpResponse;
    }
}

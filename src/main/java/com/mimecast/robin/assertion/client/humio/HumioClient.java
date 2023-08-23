package com.mimecast.robin.assertion.client.humio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.config.assertion.external.ExternalConfig;
import com.mimecast.robin.config.assertion.external.MatchExternalClientConfig;
import com.mimecast.robin.config.assertion.external.logs.LogsExternalClientConfig;
import com.mimecast.robin.http.HttpClient;
import com.mimecast.robin.http.HttpMethod;
import com.mimecast.robin.http.HttpRequest;
import com.mimecast.robin.http.HttpResponse;
import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Factories;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.util.UIDExtractor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Humio client.
 */
public class HumioClient {
    protected static final Logger log = LogManager.getLogger(HumioClient.class);

    /**
     * Connection instance.
     */
    protected final Connection connection;

    /**
     * Assert external config instance.
     */
    protected final ExternalConfig config;

    /**
     * Transaction ID.
     */
    protected final int transactionId;

    /**
     * Constructs a new HumioClient instance.
     *
     * @param connection    Connection instance.
     * @param config        MatchExternalClientConfig instance.
     * @param transactionId Transaction ID.
     */
    public HumioClient(Connection connection, MatchExternalClientConfig config, int transactionId) {
        this.connection = connection;
        this.config = config;
        this.transactionId = transactionId;
    }

    /**
     * Runs client.
     *
     * @return Server logs.
     */
    public JSONArray run() {
        JSONArray array = new JSONArray();

        // Get request.
        String request = getRequest("acode like " + getUID());

        // Make request.
        JSONObject results = getResult(request);
        String test = "";

        // Collect result.
        if (results != null &&
                results.has("lines") &&
                results.get("lines") instanceof JSONArray &&
                !((JSONArray) results.get("lines")).isEmpty()) {

            array = convertLogs((JSONArray) results.get("lines"));
        }

        return array;
    }

    /**
     * Get UID from SMTP command response.
     *
     * @return String.
     */
    protected String getUID() {
        return UIDExtractor.getUID(connection, transactionId);
    }

    /**
     * Get endpoint.
     *
     * @return String.
     */
    protected String getEndpoint() {
        String service = ((LogsExternalClientConfig) config).getService();
        return "api/v1/repositories/" + service + "/query";
    }

    /**
     * Gets request.
     *
     * @param query Search query.
     * @return Request string.
     */
    protected String getRequest(String query) {
        Map<String, Object> request = Stream.of(
                new AbstractMap.SimpleEntry<>("queryString", query),
                new AbstractMap.SimpleEntry<>("start", "1hours"),
                new AbstractMap.SimpleEntry<>("end", "now")
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new GsonBuilder().disableHtmlEscaping().create().toJson(request);
    }

    /**
     * Gets result.
     *
     * @param content String.
     * @return Server logs.
     */
    protected JSONObject getResult(String content) {
        try {
            String url = Config.getProperties().getStringProperty("humio.url");
            String endpoint = getEndpoint();

            // Log request meta.
            log.debug("URL: {}", url + endpoint);
            log.debug("Request: {}", content);

            // Make request.
            String response = makeRequest(content, url + endpoint).trim();
            log.debug("Response: {}", response);

            // Return JSONObject is JSON.
            if (response.startsWith("{") && response.endsWith("}")) {
                return new JSONObject(Collections.singletonMap("lines", response.split("\n")));
            }
        } catch (Exception e) {
            log.info("Error reading logs: {}", e.getMessage());
        }

        // Failsafe.
        return new JSONObject();
    }

    /**
     * Make request.
     *
     * @param content Content.
     * @param url     Request URL.
     * @return ResponseBody.
     * @throws IOException              Unable to communicate.
     * @throws KeyManagementException   Key management exception.
     * @throws NoSuchAlgorithmException No such algorithm exception.
     */
    protected String makeRequest(String content, String url) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        String auth = "";
        if (Config.getProperties() != null) {
            auth = Config.getProperties().getStringProperty("humio.auth", "");
        }

        HttpRequest request = new HttpRequest(url, HttpMethod.POST)
                .addContent(content, "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Authorization", "Bearer " + auth);

        HttpResponse response = new HttpClient(
                new BasicConfig(Config.getProperties().getMapProperty("humio")),
                Factories.getTrustManager()
        ).execute(request);

        log.debug("Headers: {}", response.getHeaders());

        return response.getBody();
    }

    /**
     * Convert logs into log4j lines array.
     *
     * @param results Server logs.
     * @return Server logs.
     */
    @SuppressWarnings("unchecked")
    protected JSONArray convertLogs(JSONArray results) {
        JSONArray array = new JSONArray();

        for (Object entry : results.toList()) {
            Map<String, String> map = new Gson().fromJson((String) entry, Map.class);

            array.put(String.join("|",
                    map.get("level"),
                    map.get("thread"),
                    map.get("class"),
                    map.get("acode"),
                    map.get("bcode"),
                    map.get("ccode"),
                    map.get("mimemessage")
            ));
        }

        return array;
    }
}

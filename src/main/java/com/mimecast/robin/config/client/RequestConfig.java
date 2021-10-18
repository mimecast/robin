package com.mimecast.robin.config.client;

import com.mimecast.robin.config.ConfigFoundation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.mail.internet.InternetHeaders;
import java.util.HashMap;
import java.util.Map;

/**
 * Request case configuration container.
 */
@SuppressWarnings("unchecked")
public class RequestConfig extends ConfigFoundation {

    /**
     * Constructs a new RequestConfig instance with given map.
     *
     * @param request Map.
     */
    @SuppressWarnings("rawtypes")
    public RequestConfig(Map request) {
        super(request);
    }

    /**
     * Gets request URL.
     *
     * @return URL string..
     */
    public String getUrl() {
        return getStringProperty("url");
    }

    /**
     * Gets request type.
     *
     * @return GET/POST.
     */
    public String getType() {
        return getStringProperty("type");
    }

    /**
     * Gets request headers.
     *
     * @return InternetHeaders instance.
     */
    public InternetHeaders getHeaders() {
        InternetHeaders internetHeaders = new InternetHeaders();

        for (Object object : getListProperty("headers")) {
            if (object instanceof Map) {
                Map<String, String> header = (Map<String, String>) object;
                if (header.size() > 1) {
                    internetHeaders.addHeader(header.get("name"), header.get("value"));
                }
            }
        }

        return internetHeaders;
    }

    /**
     * Gets POST parameters.
     *
     * @return Map of String, String.
     */
    public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();

        for (Object object : getListProperty("params")) {
            if (object instanceof Map) {
                Map<String, String> param = (Map<String, String>) object;
                if (param.size() > 1) {
                    params.put(param.get("name"), param.get("value"));
                }
            }
        }

        return params;
    }

    /**
     * Gets POST content.
     *
     * @return Map of String, String.
     */
    @SuppressWarnings("rawtypes")
    public Pair<String, String> getContent() {
        Pair<String, String> content = null;

        Map map = getMapProperty("content");
        if (map != null && map.containsKey("payload")) {
            String payload = (String) map.get("payload");
            String mimeType = map.containsKey("mimeType") ? (String) map.get("mimeType") : "application/json";
            content = new ImmutablePair<>(payload, mimeType);
        }

        return content;
    }

    /**
     * Gets POST files.
     *
     * @return Map of String, String.
     */
    public Map<String, String> getFiles() {
        Map<String, String> params = new HashMap<>();

        for (Object object : getListProperty("files")) {
            if (object instanceof Map) {
                Map<String, String> param = (Map<String, String>) object;
                if (param.size() > 1) {
                    params.put(param.get("name"), param.get("value"));
                }
            }
        }

        return params;
    }
}

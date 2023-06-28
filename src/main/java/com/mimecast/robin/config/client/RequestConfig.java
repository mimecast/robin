package com.mimecast.robin.config.client;

import com.google.gson.Gson;
import com.mimecast.robin.config.ConfigFoundation;
import com.mimecast.robin.smtp.io.LineInputStream;
import com.mimecast.robin.smtp.io.MagicInputStream;
import com.mimecast.robin.smtp.session.Session;
import com.mimecast.robin.util.PathUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.mail.internet.InternetHeaders;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Request case configuration container.
 */
@SuppressWarnings("unchecked")
public class RequestConfig extends ConfigFoundation {

    /**
     * Session instance.
     */
    private final Session session;

    /**
     * Constructs a new RequestConfig instance with given map.
     *
     * @param request Map.
     * @param session Session instance.
     */
    @SuppressWarnings("rawtypes")
    public RequestConfig(Map request, Session session) {
        super(request);
        this.session = session;
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
     * Gets POST content as a string.
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
     * Gets POST content as a byte array of an object loaded from JSON.
     *
     * @return Map of String, String.
     */
    @SuppressWarnings("rawtypes")
    public Pair<byte[], String> getObject() {
        Pair<byte[], String> object = null;

        Map map = getMapProperty("object");
        if (map != null && map.containsKey("path")) {
            String path = (String) map.get("path");
            byte[] bytes = new byte[0];
            if (PathUtils.isFile(path)) {
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

                    StringBuilder json = new StringBuilder();
                    LineInputStream stream = new LineInputStream(new MagicInputStream(new FileInputStream(path)));
                    while ((bytes = stream.readLine()) != null) {
                        json.append(session.magicReplace(new String(bytes)));
                    }

                    objectOutputStream.writeObject(new Gson().fromJson(json.toString(), Map.class));
                    bytes = byteArrayOutputStream.toByteArray();
                } catch (IOException e) {
                    log.error("File not found: {}", path);
                }
            }

            String mimeType = map.containsKey("mimeType") ? (String) map.get("mimeType") : "application/binary";
            object = new ImmutablePair<>(bytes, mimeType);
        }

        return object;
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

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
     * InternetHeaders instance.
     */
    private InternetHeaders internetHeaders = new InternetHeaders();

    /**
     * Params container.
     */
    private Map<String, String> params = new HashMap<>();

    /**
     * Content container.
     */
    private Pair<String, String> content = null;

    /**
     * Object container.
     */
    private Pair<byte[], String> object = null;

    /**
     * Files container.
     */
    private Map<String, String> files = new HashMap<>();

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
        if (!internetHeaders.getAllHeaders().asIterator().hasNext()) {
            for (Object object : getListProperty("headers")) {
                if (object instanceof Map) {
                    Map<String, String> header = (Map<String, String>) object;
                    if (header.size() > 1) {
                        internetHeaders.addHeader(header.get("name"), header.get("value"));
                    }
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
        if (params.isEmpty()) {
            for (Object object : getListProperty("params")) {
                if (object instanceof Map) {
                    Map<String, String> param = (Map<String, String>) object;
                    if (param.size() > 1) {
                        params.put(param.get("name"), param.get("value"));
                    }
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
        if (content == null) {
            Map map = getMapProperty("content");

            if (map != null) {
                String mimeType = map.containsKey("mimeType") ? (String) map.get("mimeType") : "application/json";

                if (map.containsKey("path")) {
                    content = new ImmutablePair<>(getFile((String) map.get("path")), mimeType);
                }
                else if (map.containsKey("payload")) {
                    content = new ImmutablePair<>((String) map.get("payload"), mimeType);
                }
            }
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
        if (object == null) {
            Map map = getMapProperty("object");
            if (map != null && map.containsKey("path")) {
                String path = (String) map.get("path");
                byte[] bytes = new byte[0];
                if (PathUtils.isFile(path)) {
                    try {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

                        objectOutputStream.writeObject(new Gson().fromJson(getFile(path), Map.class));
                        bytes = byteArrayOutputStream.toByteArray();
                    } catch (IOException e) {
                        log.error("Unable to build object: {}", e.getMessage());
                    }
                }

                String mimeType = map.containsKey("mimeType") ? (String) map.get("mimeType") : "application/binary";
                object = new ImmutablePair<>(bytes, mimeType);
            }
        }

        return object;
    }

    /**
     * Gets file content with magic applied to each line.
     *
     * @return String.
     */
    public String getFile(String path) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            LineInputStream stream = new LineInputStream(new MagicInputStream(new FileInputStream(path)));
            byte[] bytes;
            while ((bytes = stream.readLine()) != null) {
                stringBuilder.append(session.magicReplace(new String(bytes)));
            }
        } catch (IOException e) {
            log.error("Unable to read file {} due to {}", path, e.getMessage());
        }

        return stringBuilder.toString();
    }

    /**
     * Gets POST files.
     *
     * @return Map of String, String.
     */
    public Map<String, String> getFiles() {
        if (files.isEmpty()) {
            for (Object object : getListProperty("files")) {
                if (object instanceof Map) {
                    Map<String, String> file = (Map<String, String>) object;
                    if (file.size() > 1) {
                        files.put(file.get("name"), file.get("value"));
                    }
                }
            }
        }

        return files;
    }
}

package com.mimecast.robin.http;

import com.google.gson.Gson;
import com.mimecast.robin.config.BasicConfig;
import okhttp3.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP/S client.
 */
public class HttpClient {
    protected static final Logger log = LogManager.getLogger(HttpClient.class);

    /**
     * Permissive trust manager.
     */
    private final X509TrustManager trustManager;

    /**
     * Confing instance.
     */
    private final BasicConfig config;

    /**
     * Constructs a new HttpClient instance.
     *
     * @param config       Config instance.
     * @param trustManager PermissiveTrustManager instance.
     */
    public HttpClient(BasicConfig config, X509TrustManager trustManager) {
        this.config = config;
        this.trustManager = trustManager;
    }

    /**
     * Executes request.
     *
     * @param request HttpRequest instance.
     * @return HttpResponse instance.
     * @throws IOException              Unable to communicate.
     * @throws KeyManagementException   Key management exception.
     * @throws NoSuchAlgorithmException No such algorithm exception.
     */
    public HttpResponse execute(HttpRequest request) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, new TrustManager[]{trustManager}, null);

        try (Response response = getBuilder(sslContext.getSocketFactory())
                .build()
                .newCall(getRequest(request))
                .execute()) {

            HttpResponse httpResponse = new HttpResponse()
                    .setSuccess(response.isSuccessful()); // Set success.

            // Add headers.
            response.headers().toMultimap().forEach((key, value) -> value.forEach(v -> httpResponse.addHeader(key, v)));

            // Add body.
            String body = "";
            if (response.body() != null && response.body().contentType() != null) {
                if (response.body().contentType().toString().equals("application/binary")) {
                    httpResponse.addBody(new Gson().toJson(new ObjectInputStream(response.body().byteStream()).readAllBytes()));
                } else {
                    httpResponse.addBody(response.body().string());
                }
            }

            return httpResponse;
        }
    }

    /**
     * Gets Request.
     *
     * @param request HttpRequest instance.
     * @return Request instance.
     */
    private Request getRequest(HttpRequest request) {
        Request.Builder builder = new Request.Builder()
                .url(request.getUrl());

        // Add headers.
        request.getHeaders().forEach(builder::addHeader);

        FormBody.Builder form = new FormBody.Builder();
        request.getParams().forEach(form::add); // Add form data params.

        // HTTP/S DELETE
        if (request.getMethod().equals(HttpMethod.DELETE)) {
            builder.delete();
        }

        // HTTP/S POST/PUT
        else if (request.getMethod().equals(HttpMethod.POST) || request.getMethod().equals(HttpMethod.PUT)) {
            // JSON.
            if (request.getContent() != null) {
                if (request.getMethod().equals(HttpMethod.PUT)) {
                    builder.put(RequestBody.create(request.getContent().getKey(), MediaType.parse(request.getContent().getValue())));
                } else {
                    builder.post(RequestBody.create(request.getContent().getKey(), MediaType.parse(request.getContent().getValue())));
                }
            }

            // Object
            else if (request.getObject() != null) {
                if (request.getMethod().equals(HttpMethod.PUT)) {
                    builder.put(RequestBody.create(request.getObject().getKey(), MediaType.parse(request.getObject().getValue())));
                } else {
                    builder.post(RequestBody.create(request.getObject().getKey(), MediaType.parse(request.getObject().getValue())));
                }
            }

            // Multipart request if files provided.
            else if (!request.getFiles().isEmpty()) {
                MultipartBody.Builder multipart = new MultipartBody.Builder();
                multipart.setType(MultipartBody.FORM);

                request.getFiles().forEach((key, value) -> multipart.addFormDataPart(key, FilenameUtils.getName(value.getValue()),
                        RequestBody.create(new File(value.getKey()), MediaType.parse(value.getValue()))));

                request.getParams().forEach(multipart::addFormDataPart); // Add multipart data params.

                if (request.getMethod().equals(HttpMethod.PUT)) {
                    builder.put(multipart.build());
                } else {
                    builder.post(multipart.build());
                }
            }

            // Simple form data.
            else {
                if (request.getMethod().equals(HttpMethod.PUT)) {
                    builder.put(form.build());
                } else {
                    builder.post(form.build());
                }
            }
        }

        // HTTP/S GET
        else {
            builder.get();
        }

        return builder.build();
    }

    /**
     * Gets OkHttpClient.Builder.
     * <p>Isolated for testing.
     * <p>Disabled redirects per RFC specification.
     *
     * @param socketFactory SSLSocketFactory instance.
     * @return OkHttpClient.Builder instance.
     */
    private OkHttpClient.Builder getBuilder(SSLSocketFactory socketFactory) {
        return new OkHttpClient.Builder()
                .connectTimeout(config.getLongProperty("connectTimeout", 10L), TimeUnit.SECONDS)
                .writeTimeout(config.getLongProperty("writeTimeout", 10L), TimeUnit.SECONDS)
                .readTimeout(config.getLongProperty("readTimeout", 30L), TimeUnit.SECONDS)
                .sslSocketFactory(socketFactory, trustManager)
                .followRedirects(true)
                .followSslRedirects(true);
    }
}

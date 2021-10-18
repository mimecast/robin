package com.mimecast.robin.http;

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
     * Constructs a new FfsClient instance.
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

        Response response = getBuilder(sslContext.getSocketFactory())
                .build()
                .newCall(getRequest(request))
                .execute();

        HttpResponse httpResponse = new HttpResponse()
                .setSuccess(response.isSuccessful()); // Set success.

        // Add headers.
        response.headers().toMultimap().forEach((key, value) -> value.forEach(v -> httpResponse.addHeader(key, v)));

        return response.body() != null ? // Add body.
                httpResponse.addBody(response.body().string()) :
                httpResponse;
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

        // HTTP/S POST
        if (request.getMethod().equals(HttpMethod.POST)) {
            // JSON post.
            if (request.getContent() != null) {
                builder.post(RequestBody.create(request.getContent().getKey(), MediaType.parse(request.getContent().getValue())));
            }

            // Multipart request if files provided.
            else if (!request.getFiles().isEmpty()) {
                MultipartBody.Builder multipart = new MultipartBody.Builder();
                multipart.setType(MultipartBody.FORM);

                request.getFiles().forEach((key, value) -> multipart.addFormDataPart(key, FilenameUtils.getName(value.getValue()),
                        RequestBody.create(new File(value.getKey()), MediaType.parse(value.getValue()))));

                request.getParams().forEach(multipart::addFormDataPart); // Add multipart data params.
                builder.post(multipart.build());

            }

            // Simple form data.
            else {
                FormBody.Builder form = new FormBody.Builder();
                request.getParams().forEach(form::add); // Add form data params.
                builder.post(form.build());
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
                .followRedirects(false)
                .followSslRedirects(false);
    }
}

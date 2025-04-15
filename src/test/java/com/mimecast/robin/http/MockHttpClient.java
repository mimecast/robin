package com.mimecast.robin.http;

import com.mimecast.robin.config.BasicConfig;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

class MockHttpClient extends HttpClient{

    Response mockResponse;
    public MockHttpClient(BasicConfig config, X509TrustManager trustManager, Response mockResponse) {
        super(config, trustManager);
        this.mockResponse = mockResponse;
    }

    @Override
    protected OkHttpClient getClient(SSLSocketFactory socketFactory) {
        return new MockOkHttpClient(mockResponse);
    }
}

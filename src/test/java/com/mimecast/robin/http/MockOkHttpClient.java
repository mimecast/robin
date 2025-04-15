package com.mimecast.robin.http;

import okhttp3.*;
import okio.Timeout;
import org.jetbrains.annotations.NotNull;

public class MockOkHttpClient extends OkHttpClient {
    Response response;

    public MockOkHttpClient(Response response) {
        this.response = response;
    }

    @NotNull
    @Override
    public Call newCall(@NotNull Request request) {
        return new MockCall(response);
    }
}

class MockCall implements Call {

    Response response;

    public MockCall(Response response) {
        this.response = response;
    }

    @Override
    public void cancel() {

    }

    @NotNull
    @Override
    public Request request() {
        return response.request();
    }

    @NotNull
    @Override
    public Response execute() {
        return response;
    }

    @Override
    public void enqueue(@NotNull Callback callback) {

    }

    @Override
    public boolean isExecuted() {
        return true;
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @NotNull
    @Override
    public Timeout timeout() {
        return new Timeout();
    }

    @NotNull
    @Override
    public Call clone() {
        return null;
    }
}

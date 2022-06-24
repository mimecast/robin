package com.mimecast.robin.assertion.client.logs;

public class LogsExternalClientMock extends LogsExternalClient {

    /**
     * Constructs a new LogsExternalClientMock instance.
     */
    public LogsExternalClientMock() {
        this.dir = "src/test/resources/";
        this.file = dir + "sample.log";
    }
}

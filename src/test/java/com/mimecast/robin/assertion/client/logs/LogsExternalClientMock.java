package com.mimecast.robin.assertion.client.logs;

import java.nio.file.Paths;

public class LogsExternalClientMock extends LogsExternalClient {

    /**
     * Constructs a new LogsExternalClientMock instance.
     */
    public LogsExternalClientMock() {
        this.dir = "src/test/resources/";
        this.path = Paths.get(dir, "sample.log").toString();
    }

    @Override
    protected void setPath(String fileName) {
        // Do nothing
    }
}

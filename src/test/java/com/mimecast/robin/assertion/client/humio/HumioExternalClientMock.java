package com.mimecast.robin.assertion.client.humio;

class HumioExternalClientMock extends HumioExternalClient {

    /**
     * Gets new HumioClient.
     *
     * @return HumioClientMock instance.
     */
    @Override
    protected HumioClient getClient() {
        return new HumioClientMock(connection, config, transactionId);
    }
}
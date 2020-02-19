package com.mimecast.robin.storage;

import com.mimecast.robin.smtp.connection.Connection;

import java.io.FileNotFoundException;
import java.io.OutputStream;

/**
 * Interface for MTA file storage integration.
 * <p>The instanciation of this will be done via Factories.
 * <p>Connection is required to allow customisation absed on sender/recipient.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public interface StorageClient {

    /**
     * Sets connection.
     *
     * @param connection Connection instance.
     * @return Self.
     */
    StorageClient setConnection(Connection connection);

    /**
     * Gets file output stream.
     *
     * @return OutputStream instance.
     */
    OutputStream getStream() throws FileNotFoundException;

    /**
     * Gets file token.
     *
     * @return String.
     */
    String getToken();

    /**
     * Saves file.
     */
    void save();
}

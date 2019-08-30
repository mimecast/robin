package com.mimecast.robin.smtp.io;

import java.io.IOException;

/**
 * InputStream interface.
 * <p>This provides a means to read lines binary ready.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public interface InputStream {

    /**
     * Read line as byte array.
     *
     * @return Byte array.
     * @throws IOException Unable to read.
     */
    byte[] readLine() throws IOException;
}

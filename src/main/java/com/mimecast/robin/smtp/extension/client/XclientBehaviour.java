package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.session.XclientSession;

import java.io.IOException;

/**
 * Client behaviour with XCLIENT support.
 *
 * @see <a href="http://www.postfix.org/XCLIENT_README.html">Postfix XCLIENT</a>
 */
public class XclientBehaviour extends DefaultBehaviour {

    /**
     * Executes delivery.
     */
    @Override
    public void process(Connection connection) throws IOException {
        this.connection = connection;

        if (!ehlo()) return;
        if (!startTls()) return;

        // XCLIENT
        if (connection.getSession() instanceof XclientSession) {
            XclientSession session = (XclientSession) connection.getSession();
            if (session.getXclient() != null && session.getXclient().size() > 0) {
                if (!process("xclient", connection)) return;

                // Post XCLIENT hello.
                if (!ehlo()) return;
            }
        }

        if (!auth()) return;
        if (!connection.getSession().getEnvelopes().isEmpty()) {
            data();
        }
        quit();
    }
}

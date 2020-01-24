package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.session.XclientSession;

import java.io.IOException;
import java.util.Map;

/**
 * XCLIENT extension processor.
 * @link http://www.postfix.org/XCLIENT_README.html Postfix XCLIENT
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class ClientXclient extends ClientProcessor {

    /**
     * XCLIENT processor.
     * <p>Parameters: NAME, ADDR, PORT, PROTO, HELO, LOGIN, DESTADDR, DESTPORT
     *
     * @param connection Connection instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean process(Connection connection) throws IOException {
        super.process(connection);

        final String client = "XCLIENT";

        StringBuilder params = new StringBuilder();
        for (Object o : ((XclientSession) connection.getSession()).getXclient().entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            params.append(" ").append(pair.getKey()).append("=").append(pair.getValue());
        }
        connection.write(client + params.toString());

        String read = connection.read("220");

        connection.getSessionTransactionList().addTransaction(client, client + params, read, !read.startsWith("220"));
        connection.getSession().setEhloLog("XHLO");

        return read.startsWith("220");
    }
}

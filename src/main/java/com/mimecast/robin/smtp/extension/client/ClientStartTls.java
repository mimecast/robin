package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.connection.SmtpException;

import java.io.IOException;

/**
 * STARTTLS extension processor.
 */
public class ClientStartTls extends ClientProcessor {

    /**
     * STARTTLS processor.
     *
     * @param connection Connection instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection) throws IOException {
        super.process(connection);

        if (connection.getSession().isTls() && connection.getSession().isEhloTls() && !connection.getSession().isStartTls()) {
            String write = "STARTTLS";
            connection.write(write);

            String read = connection.read("220");

            connection.getSessionTransactionList().addTransaction(write, write, read, !read.startsWith("220"));
            if (!read.startsWith("220")) throw new SmtpException("STARTTLS");

            connection.setProtocols(connection.getSession().getProtocols());
            connection.setCiphers(connection.getSession().getCiphers());

            try {
                connection.startTLS(true);
                connection.getSessionTransactionList().addTransaction("TLS", "",
                        connection.getProtocol() + ":" + connection.getCipherSuite(), false);
            } catch (SmtpException e) {
                connection.getSessionTransactionList().addTransaction("TLS", "",
                        e.getCause().getMessage(), true);
                throw e;
            }

            connection.getSession().setStartTls(true);
            connection.buildStreams();
            connection.getSession().setEhloLog("SHLO");
        }

        return true;
    }
}

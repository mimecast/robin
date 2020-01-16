package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.smtp.connection.SmtpException;
import com.mimecast.robin.smtp.connection.Connection;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;

/**
 * EHLO extension processor.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class ClientEhlo extends ClientProcessor {

    /**
     * EHLO processor.
     *
     * @param connection Connection instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection) throws IOException {
        this.connection = connection;

        return sendEhlo();
    }

    /**
     * Send EHLO.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean sendEhlo() throws IOException {
        String write = getEhlo();

        connection.write(write);
        String read = connection.read("250");

        processResponse(write, read);
        processFeatures(read);

        return read.startsWith("250");
    }

    /**
     * Gets EHLO.
     *
     * @return EHLO string.
     */
    @SuppressWarnings("WeakerAccess")
    public String getEhlo() {
        String write = "EHLO ";

        if (StringUtils.isNotBlank(connection.getSession().getEhlo())) {
            write += connection.getSession().getEhlo();
        } else {
            try {
                write += InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                write += "localhost";
                log.warn("Unable to get hostname for {} using localhost.", e.getMessage());
            }
        }

        return write;
    }

    /**
     * Process EHLO response.
     *
     * @param write Write string.
     * @param read Read string.
     * @throws SmtpException SMTP delivery exception.
     */
    private void processResponse(String write, String read) throws SmtpException {
        connection.getSessionTransactionList().addTransaction(connection.getSession().getEhloLog(), write, read, !read.startsWith("250"));

        if (read.isEmpty()) throw new SmtpException("EHLO");
    }

    /**
     * Process EHLO advertised features.
     *
     * @param read Read string.
     */
    private void processFeatures(String read) {
        for (String line : read.split("\r\n")) {
            line = line.replace("250 ", "").replace("250-", "").toLowerCase();

            if(line.startsWith("size ")) {
                connection.getSession().setEhloSize(Integer.parseInt(line.replace("size ", "").trim()));
            }

            if(line.startsWith("auth ")) {
                Collections.addAll(connection.getSession().getEhloAuth(), line.replace("^auth", "").trim().split("\\s"));
            }

            if (line.contains("8bitmime"))   connection.getSession().setEhlo8bit(true);
            if (line.contains("binarymime")) connection.getSession().setEhloBinary(true);
            if (line.contains("chunking"))   connection.getSession().setEhloBdat(true);
            if (line.contains("starttls"))   connection.getSession().setEhloTls(true);
        }
    }
}

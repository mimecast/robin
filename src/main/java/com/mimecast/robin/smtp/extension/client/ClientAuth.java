package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.main.Factories;
import com.mimecast.robin.smtp.auth.DigestMD5Client;
import com.mimecast.robin.smtp.auth.Login;
import com.mimecast.robin.smtp.auth.Plain;
import com.mimecast.robin.smtp.connection.Connection;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * AUTH extension processor.
 */
public class ClientAuth extends ClientProcessor {

    /**
     * AUTH mechanisms.
     */
    private static final String AUTH_DIGEST = "AUTH DIGEST-MD5";
    private static final String AUTH_PLAIN = "AUTH PLAIN";
    private static final String AUTH_LOGIN = "AUTH LOGIN";

    /**
     * AUTH LOGIN configuration.
     */
    private boolean authLoginCombined = false;
    private boolean authLoginRetry = false;

    /**
     * AUTH processor.
     *
     * @param connection Connection instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection) throws IOException {
        super.process(connection);

        // Configure
        authLoginCombined = connection.getSession().isAuthLoginCombined();
        authLoginRetry = connection.getSession().isAuthLoginRetry();

        // Select mechanism.
        if (connection.getSession().getEhloAuth().contains("digest-md5")) {
            return authDigestMD5();
        } else if (connection.getSession().getEhloAuth().contains("login")) {
            return authLogin();
        } else if (connection.getSession().getEhloAuth().contains("plain")) {
            return authPlain();
        }

        return false;
    }

    /**
     * DIGEST-MD5 client instance factory.
     *
     * @return DigestMD5Client instance.
     */
    DigestMD5Client digestMD5ClientFactory() {
        DigestMD5Client digestMd5 = new DigestMD5Client(
                connection.getSession().getEhlo(),
                connection.getSession().getUsername(),
                connection.getSession().getPassword(),
                connection.getSession().getEhlo());
        digestMd5.setDigestDatabase(Factories.getDatabase());
        return digestMd5;
    }

    /**
     * DIGEST-MD5 mechanism.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    private boolean authDigestMD5() throws IOException {
        DigestMD5Client digestMd5 = digestMD5ClientFactory();

        // Auth command.
        String write = AUTH_DIGEST;

        // Vars.
        String challenge = "";
        String response = "";

        // Subsequent authentication.
        String subsequentAuthentication = digestMd5.getSubsequentAuthentication();
        if (StringUtils.isNotBlank(subsequentAuthentication)) {
            response = subsequentAuthentication;
            write += " " + response;
        }

        // Send initial.
        connection.write(write);
        String read = connection.read();

        // If no subsequent authentication process challenge.
        if (read.startsWith("334")) {
            challenge = read.substring(4);

            response = digestMd5.authenticateClient(challenge);
            connection.write(response);
            read = connection.read("334");
            String server = read.substring(4);

            if (!read.startsWith("334")) {
                connection.getSession().getSessionTransactionList().addTransaction("AUTH", challenge + "/" + response, read, true);
                return false;
            }

            if (!digestMd5.authenticateServer(server)) {
                connection.getSession().getSessionTransactionList().addTransaction("AUTH", challenge + "/" + response, read);
                return false;
            }
            connection.write("");
            read = connection.read("235");
        }

        connection.getSession().getSessionTransactionList().addTransaction("AUTH", challenge + "/" + response, read, !read.startsWith("235"));
        return read.startsWith("235");
    }

    /**
     * LOGIN mechanism.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    private boolean authLogin() throws IOException {
        Login login = new Login(connection);

        String write = AUTH_LOGIN + " " + login.getUsername();

        // Try to send both username and password.
        if (authLoginCombined) {
            write += " " + login.getPassword();
        }

        connection.write(write);
        String read = connection.read("334");

        // If unexpected response fail.
        if (!read.startsWith("235") && !read.startsWith("334")) {
            // If retry do without authLoginCombined.
            if (authLoginRetry) {
                authLoginRetry = authLoginCombined = false;
                return authLogin();
            }
            connection.getSession().getSessionTransactionList().addTransaction("AUTH", write, read, true);
            return false;
        }

        // If password requested send again.
        if (read.startsWith("334")) {
            connection.write(login.getPassword());
            read = connection.read("235");

        }

        connection.getSession().getSessionTransactionList().addTransaction("AUTH", write + " " + login.getPassword(), read, !read.startsWith("235"));
        return read.startsWith("235");
    }

    /**
     * PLAIN mechanism.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    private boolean authPlain() throws IOException {
        Plain plain = new Plain(connection);
        String write = AUTH_PLAIN + " " + plain.getLogin();

        connection.write(write);
        String read = connection.read("235");
        connection.getSession().getSessionTransactionList().addTransaction("AUTH", write, read, !read.startsWith("235"));
        return read.startsWith("235");
    }
}

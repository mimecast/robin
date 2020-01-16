package com.mimecast.robin.smtp.extension.server;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Extensions;
import com.mimecast.robin.config.server.UserConfig;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.verb.AuthVerb;
import com.mimecast.robin.smtp.verb.Verb;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.Optional;

/**
 * AUTH extension processor.
 * TODO Implement DIGEST-MD5 support.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class ServerAuth extends ServerProcessor {

    /**
     * Advert getter.
     *
     * @return Advert string.
     */
    @Override
    public String getAdvert() {
        return Config.getServer().isAuth() ? "AUTH PLAIN LOGIN" : "";
    }

    /**
     * AUTH processor.
     *
     * @param connection Connection instance.
     * @param verb       Verb instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection, Verb verb) throws IOException {
        super.process(connection, verb);

        AuthVerb authVerb = new AuthVerb(verb);
        if (verb.getCount() > 1) {
            switch (authVerb.getType()) {
                case "PLAIN":
                    processAuthPlain(verb);
                    break;

                case "LOGIN":
                    processAuthLogin(verb);
                    break;

                default:
                    break;
            }

            // Get available users for authentication.
            if (!connection.getSession().getUsername().isEmpty()) {
                Optional<UserConfig> opt = connection.getUser(connection.getSession().getUsername());
                if (opt.isPresent() && opt.get().getPass().equals(connection.getSession().getPassword())) {
                    connection.getSession().setAuth(true);
                    connection.write("235 2.7.0 Authorized");
                    return true;
                } else {
                    connection.write("535 5.7.1 Unauthorized");
                    return false;
                }
            }
        }

        connection.write("504 5.7.4 Unrecognized authentication type");
        return false;
    }

    /**
     * Process auth plain.
     *
     * @param verb Verb instance.
     * @throws IOException Unable to communicate.
     */
    private void processAuthPlain(Verb verb) throws IOException {
        String auth;

        if (verb.getCount() == 2) {
            connection.write("334 UGF5bG9hZDo"); // Payload:

            auth = connection.read();
            if (Extensions.isExtension(auth)) return; // Failsafe to catch unexpected commands.

            String decoded = new String(Base64.decodeBase64(auth));
            String[] parts = decoded.split("\u0000");
            if (parts.length == 3) {
                connection.getSession().setUsername(parts[1]);
                connection.getSession().setPassword(parts[2]);
            }
        }
    }

    /**
     * Process auth login.
     *
     * @param verb Verb instance.
     * @throws IOException Unable to communicate.
     */
    private void processAuthLogin(Verb verb) throws IOException {
        String user;
        String pass;

        if (verb.getCount() > 2) {
            user = verb.getPart(2);
            user = new String(Base64.decodeBase64(user));
            if (Extensions.isExtension(user)) return; // Failsafe to catch unexpected commands.
            connection.write("334 UGFzc3dvcmQ6"); // Password:

            pass = connection.read();
            pass = new String(Base64.decodeBase64(pass));
            if (Extensions.isExtension(pass)) return; // Failsafe to catch unexpected commands.
        }

        else {
            connection.write("334 VXNlcm5hbWU6"); // Username:
            user = connection.read();
            user = new String(Base64.decodeBase64(user));
            if (Extensions.isExtension(user)) return; // Failsafe to catch unexpected commands.

            connection.write("334 UGFzc3dvcmQ6"); // Password:
            pass = connection.read();
            pass = new String(Base64.decodeBase64(pass));
            if (Extensions.isExtension(pass)) return; // Failsafe to catch unexpected commands.
        }

        connection.getSession().setUsername(user);
        connection.getSession().setPassword(pass);
    }
}

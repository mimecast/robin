package com.mimecast.robin.smtp.extension.server;

import com.mimecast.robin.config.server.ScenarioConfig;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.verb.Verb;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * RCPT extension processor.
 */
public class ServerRcpt extends ServerMail {

    /**
     * RCPT processor.
     *
     * @param connection Connection instance.
     * @param verb       Verb instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection, Verb verb) throws IOException {
        super.process(connection, verb);

        // Scenario response.
        Optional<ScenarioConfig> opt = connection.getScenario();
        if (opt.isPresent() && opt.get().getRcpt() != null) {
            for (Map<String, String> entry : opt.get().getRcpt()) {
                if (getAddress() != null && getAddress().getAddress().matches(entry.get("value"))) {
                    String response = entry.get("response");
                    if (response.startsWith("2")) {
                        connection.getSession().addRcpt(getAddress());
                    }
                    connection.write(response);
                    return response.startsWith("2");
                }
            }
        }

        // Accept all.
        connection.getSession().addRcpt(getAddress());
        connection.write("250 2.1.5 Recipient OK [" + connection.getSession().getUID() + "]");

        return true;
    }

    /**
     * Gets RCPT TO address.
     *
     * @return Address instance.
     * @throws IOException RCPT address parsing problem.
     */
    @Override
    public InternetAddress getAddress() throws IOException {
        if (address == null) {
            try {
                address = new InternetAddress(verb.getParam("to"));
            } catch (AddressException e) {
                throw new IOException(e);
            }
        }

        return address;
    }
}

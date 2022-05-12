package com.mimecast.robin.smtp.extension.server;

import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.config.server.ScenarioConfig;
import com.mimecast.robin.main.Config;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.verb.Verb;

import java.io.IOException;
import java.util.Optional;

/**
 * STARTLS extension processor.
 */
public class ServerStartTls extends ServerProcessor {

    /**
     * STARTTLS advert.
     *
     * @return Advert string.
     */
    @Override
    public String getAdvert() {
        return Config.getServer().isStartTls() ? "STARTTLS" : "";
    }

    /**
     * STARTTLS processor.
     *
     * @param connection Connection instance.
     * @param verb       Verb instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean process(Connection connection, Verb verb) throws IOException {
        super.process(connection, verb);

        boolean shakeHand = true;
        String handShake = "220 Ready for handshake [" + connection.getSession().getUID() + "]";

        // ScenarioConfig response.
        Optional<ScenarioConfig> opt = connection.getScenario();
        if (opt.isPresent() && opt.get().getStarTls() != null && !opt.get().getStarTls().isEmpty()) {
            BasicConfig tls = opt.get().getStarTls();
            if (!tls.isEmpty()) {
                if (tls.hasProperty("response")) {
                    handShake = tls.getStringProperty("response");
                    shakeHand = handShake.startsWith("2");
                }

                if (tls.hasProperty("protocols")) {
                    connection.setProtocols((String[]) tls.getListProperty("protocols").toArray(new String[0]));
                }

                if (tls.hasProperty("ciphers")) {
                    connection.setCiphers((String[]) tls.getListProperty("ciphers").toArray(new String[0]));
                }
            }
        }

        // Shake hand.
        connection.write(handShake);

        if (shakeHand) {
            connection.startTLS(false);
            connection.getSession().setStartTls(true);
            connection.buildStreams();
        }

        return true;
    }
}

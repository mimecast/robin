package com.mimecast.robin.smtp.extension.server;

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
    public boolean process(Connection connection, Verb verb) throws IOException {
        super.process(connection, verb);

        boolean shakeHand = true;

        // ScenarioConfig response.
        Optional<ScenarioConfig> opt = connection.getScenario();
        if (opt.isPresent() && opt.get().getStarTls() != null) {
            connection.write(opt.get().getStarTls());
            shakeHand = opt.get().getStarTls().startsWith("2");
        }

        // Shake hand.
        else {
            connection.write("220 Ready for handshake");
        }

        if (shakeHand) {
            connection.startTLS(false);
            connection.getSession().setStartTls(true);
            connection.buildStreams();
        }

        return true;
    }
}

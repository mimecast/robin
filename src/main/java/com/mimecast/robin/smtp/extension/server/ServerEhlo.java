package com.mimecast.robin.smtp.extension.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mimecast.robin.config.server.ScenarioConfig;
import com.mimecast.robin.main.Extensions;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.extension.Extension;
import com.mimecast.robin.smtp.verb.EhloVerb;
import com.mimecast.robin.smtp.verb.Verb;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * EHLO extension processor.
 */
public class ServerEhlo extends ServerProcessor {

    /**
     * EHLO processor.
     *
     * @param connection Connection instance.
     * @param verb       Verb instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection, Verb verb) throws IOException {
        super.process(connection, verb);

        EhloVerb ehloVerb = new EhloVerb(verb);
        connection.getSession().setEhlo(ehloVerb.getDomain());

        // Prepare welcome message.
        String welcome = "Welcome [" + connection.getSession().getFriendRdns() + " (" + connection.getSession().getFriendAddr() + ")]";

        // ScenarioConfig response.
        Optional<ScenarioConfig> opt = connection.getScenario();
        if ("ehlo".equals(verb.getKey()) && opt.isPresent() && opt.get().getEhlo() != null) {
            connection.write(opt.get().getEhlo());
        } else if ("helo".equals(verb.getKey()) && opt.isPresent() && opt.get().getHelo() != null) {
            connection.write(opt.get().getHelo());
        }

        // HELO response.
        else if (verb.getKey().equals("helo")) {
            connection.write("250 " + welcome);
        }

        // EHLO response.
        else {
            connection.write("250-" + welcome);
            writeAdverts();
        }

        return true;
    }

    /**
     * Writes adverts to socket.
     *
     * @throws IOException Unable to communicate.
     */
    public void writeAdverts() throws IOException {
        List<String> adverts = Lists.newArrayList(Sets.newHashSet(collectAdverts()));
        for (int i = 0; i < adverts.size(); i++) {
            if (!adverts.get(i).equalsIgnoreCase("STARTLS") || !connection.getSession().isStartTls()) {
                connection.write("250" + ((adverts.size() - 1) > i ? "-" : " ") + adverts.get(i));
            }
        }
    }

    /**
     * Collects adverts from extensions.
     *
     * @return List of strings.
     */
    @SuppressWarnings("WeakerAccess")
    public static List<String> collectAdverts() {
        List<String> adverts = new ArrayList<>();
        adverts.add("PIPELINING");
        for (String s : Extensions.getExtensions().keySet()) {

            Optional<Extension> ept = Extensions.getExtension(s);
            if (ept.isPresent()) {

                String advert = ept.get().getServer().getAdvert();
                if (StringUtils.isNotBlank(advert)) {
                    adverts.add(advert);
                }
            }
        }

        return adverts;
    }
}

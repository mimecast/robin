package com.mimecast.robin.smtp.extension.server;

import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.verb.Verb;

import java.io.IOException;

/**
 * XCLIENT extension processor.
 *
 * @see <a href="http://www.postfix.org/XCLIENT_README.html">Postfix XCLIENT</a>
 */
public class ServerXclient extends ServerProcessor {

    /**
     * XCLIENT processor.
     *
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection, Verb verb) throws IOException {
        super.process(connection, verb);

        connection.getSession().setEhlo(verb.getParam("helo"));
        connection.getSession().setFriendRdns(verb.getParam("name"));
        connection.getSession().setFriendAddr(verb.getParam("addr"));
        connection.write("220 " + connection.getSession().getRdns() + " ESMTP; " + connection.getSession().getDate());

        return true;
    }
}

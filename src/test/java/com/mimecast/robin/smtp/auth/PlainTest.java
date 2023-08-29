package com.mimecast.robin.smtp.auth;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.session.Session;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlainTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void getResponse() {
        Session session = new Session();
        session.setUsername("tony@example.com");
        session.setUsername("giveHerTheRing");

        Plain plain = new Plain(new Connection(session));
        Plain plainNoSession = new Plain(new Connection(new Session()));
        assertEquals("Z2l2ZUhlclRoZVJpbmcAZ2l2ZUhlclRoZVJpbmcA", plain.getLogin());
        assertEquals("AAA=", plainNoSession.getLogin());
    }
}

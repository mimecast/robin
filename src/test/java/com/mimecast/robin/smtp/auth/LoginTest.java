package com.mimecast.robin.smtp.auth;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.session.Session;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginTest {

    private static Login authPlain;
    private static Login authPlainNoSession;

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");

        Session session = new Session();
        session.setUsername("tony@example.com");
        session.setPassword("giveHerTheRing");

        authPlain = new Login(new Connection(session));
        authPlainNoSession = new Login(new Connection(new Session()));
    }

    @Test
    void getUsername() {
        assertEquals("dG9ueUBleGFtcGxlLmNvbQ==", authPlain.getUsername());
        assertEquals("", authPlainNoSession.getUsername());
    }

    @Test
    void getPassword() {
        assertEquals("Z2l2ZUhlclRoZVJpbmc=", authPlain.getPassword());
        assertEquals("", authPlainNoSession.getPassword());
    }
}

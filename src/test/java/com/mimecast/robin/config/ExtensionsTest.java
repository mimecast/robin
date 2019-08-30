package com.mimecast.robin.config;

import com.mimecast.robin.main.Extensions;
import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.extension.Extension;
import com.mimecast.robin.smtp.extension.client.ClientProcessor;
import com.mimecast.robin.smtp.extension.server.ServerProcessor;
import com.mimecast.robin.smtp.verb.Verb;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class ExtensionsTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @AfterAll
    static void after() {
        Extensions.removeExtension("test");
    }

    @Test
    void getExtensions() {
        assertFalse(Extensions.getExtensions().isEmpty());
    }

    @Test
    void isExtensionString() {
        assertTrue(Extensions.isExtension("helo"));
        assertTrue(Extensions.isExtension("ehlo"));

        assertTrue(Extensions.isExtension("starttls"));
        assertTrue(Extensions.isExtension("auth"));

        assertTrue(Extensions.isExtension("mail"));
        assertTrue(Extensions.isExtension("rcpt"));

        assertTrue(Extensions.isExtension("data"));
        assertTrue(Extensions.isExtension("bdat"));

        assertTrue(Extensions.isExtension("rset"));
        assertTrue(Extensions.isExtension("help"));
        assertTrue(Extensions.isExtension("quit"));
    }

    @Test
    void isExtensionVerb() {
        assertTrue(Extensions.isExtension(new Verb("helo")));
        assertTrue(Extensions.isExtension(new Verb("ehlo")));

        assertTrue(Extensions.isExtension(new Verb("starttls")));
        assertTrue(Extensions.isExtension(new Verb("auth")));

        assertTrue(Extensions.isExtension(new Verb("mail")));
        assertTrue(Extensions.isExtension(new Verb("rcpt")));

        assertTrue(Extensions.isExtension(new Verb("data")));
        assertTrue(Extensions.isExtension(new Verb("bdat")));

        assertTrue(Extensions.isExtension(new Verb("rset")));
        assertTrue(Extensions.isExtension(new Verb("help")));
        assertTrue(Extensions.isExtension(new Verb("quit")));
    }

    @Test
    void getExtensionString() {
        assertNotNull(Extensions.getExtension("helo").get().getClient());
        assertNotNull(Extensions.getExtension("helo").get().getServer());
        assertNotNull(Extensions.getExtension("ehlo").get().getClient());
        assertNotNull(Extensions.getExtension("ehlo").get().getServer());

        assertNotNull(Extensions.getExtension("starttls").get().getClient());
        assertNotNull(Extensions.getExtension("starttls").get().getServer());
        assertNotNull(Extensions.getExtension("auth").get().getClient());
        assertNotNull(Extensions.getExtension("auth").get().getServer());

        assertNotNull(Extensions.getExtension("mail").get().getClient());
        assertNotNull(Extensions.getExtension("mail").get().getServer());
        assertNotNull(Extensions.getExtension("rcpt").get().getClient());
        assertNotNull(Extensions.getExtension("rcpt").get().getServer());

        assertNotNull(Extensions.getExtension("data").get().getClient());
        assertNotNull(Extensions.getExtension("data").get().getServer());
        assertNotNull(Extensions.getExtension("bdat").get().getClient());
        assertNotNull(Extensions.getExtension("bdat").get().getServer());

        assertNotNull(Extensions.getExtension("rset").get().getClient());
        assertNotNull(Extensions.getExtension("rset").get().getServer());
        assertNotNull(Extensions.getExtension("help").get().getClient());
        assertNotNull(Extensions.getExtension("help").get().getServer());
        assertNotNull(Extensions.getExtension("quit").get().getClient());
        assertNotNull(Extensions.getExtension("quit").get().getServer());
    }

    @Test
    void getExtensionVerb() {
        assertNotNull(Extensions.getExtension(new Verb("helo")).get().getClient());
        assertNotNull(Extensions.getExtension(new Verb("helo")).get().getServer());
        assertNotNull(Extensions.getExtension(new Verb("ehlo")).get().getClient());
        assertNotNull(Extensions.getExtension(new Verb("ehlo")).get().getServer());

        assertNotNull(Extensions.getExtension(new Verb("starttls")).get().getClient());
        assertNotNull(Extensions.getExtension(new Verb("starttls")).get().getServer());
        assertNotNull(Extensions.getExtension(new Verb("auth")).get().getClient());
        assertNotNull(Extensions.getExtension(new Verb("auth")).get().getServer());

        assertNotNull(Extensions.getExtension(new Verb("mail")).get().getClient());
        assertNotNull(Extensions.getExtension(new Verb("mail")).get().getServer());
        assertNotNull(Extensions.getExtension(new Verb("rcpt")).get().getClient());
        assertNotNull(Extensions.getExtension(new Verb("rcpt")).get().getServer());

        assertNotNull(Extensions.getExtension(new Verb("data")).get().getClient());
        assertNotNull(Extensions.getExtension(new Verb("data")).get().getServer());
        assertNotNull(Extensions.getExtension(new Verb("bdat")).get().getClient());
        assertNotNull(Extensions.getExtension(new Verb("bdat")).get().getServer());

        assertNotNull(Extensions.getExtension(new Verb("rset")).get().getClient());
        assertNotNull(Extensions.getExtension(new Verb("rset")).get().getServer());
        assertNotNull(Extensions.getExtension(new Verb("help")).get().getClient());
        assertNotNull(Extensions.getExtension(new Verb("help")).get().getServer());
        assertNotNull(Extensions.getExtension(new Verb("quit")).get().getClient());
        assertNotNull(Extensions.getExtension(new Verb("quit")).get().getServer());
    }

    private static class ServerTest extends ServerProcessor {}
    private static class ClientTest extends ClientProcessor {}

    @Test
    void addExtension() {
        Extensions.addExtension("test", new Extension(ServerTest::new, ClientTest::new));
        assertTrue(Extensions.isExtension("test"));
        assertTrue(Extensions.isExtension(new Verb("test")));
        assertNotNull(Extensions.getExtension("test").get().getClient());
        assertNotNull(Extensions.getExtension("test").get().getServer());
        assertNotNull(Extensions.getExtension(new Verb("test")).get().getClient());
        assertNotNull(Extensions.getExtension(new Verb("test")).get().getServer());
    }

    @Test
    void getHelp() {
        String help = Extensions.getHelp();
        assertTrue(help.contains("HELO"));
        assertTrue(help.contains("EHLO"));
        assertTrue(help.contains("STARTTLS"));
        assertTrue(help.contains("AUTH"));
        assertTrue(help.contains("MAIL"));
        assertTrue(help.contains("RCPT"));
        assertTrue(help.contains("DATA"));
        assertTrue(help.contains("BDAT"));
        assertTrue(help.contains("RSET"));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("QUIT"));
    }
}

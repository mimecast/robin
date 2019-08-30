package com.mimecast.robin.smtp.verb;

import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.ConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class VerbTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void verb() {
        Verb verb;
        verb = new Verb(new Verb("STARTTLS"));
        assertEquals("STARTTLS", verb.getVerb());

        verb = new Verb(new Verb("DATA"));
        assertEquals("DATA", verb.getVerb());

        verb = new Verb(new Verb("RESET"));
        assertEquals("RESET", verb.getVerb());

        verb = new Verb(new Verb("HELP"));
        assertEquals("HELP", verb.getVerb());

        verb = new Verb(new Verb("QUIT"));
        assertEquals("QUIT", verb.getVerb());
    }

    @Test
    void ehlo() {
        EhloVerb verb;
        verb = new EhloVerb(new Verb("EHLO example.com"));
        assertEquals(2, verb.getCount());
        assertEquals("example.com", verb.getPart(1));
        assertEquals("example.com", verb.getDomain());
    }

    @Test
    void mail() throws IOException, AddressException {
        MailVerb verb;
        verb = new MailVerb(new Verb("MAIL FROM:tony@example.com"));
        assertEquals(new InternetAddress("tony@example.com"), verb.getAddress());

        verb = new MailVerb(new Verb("MAIL FROM: tony@example.com"));
        assertEquals(new InternetAddress("tony@example.com"), verb.getAddress());

        verb = new MailVerb(new Verb("MAIL FROM:<tony@example.com> SIZE=12345 BODY=8BITMIME"));
        assertEquals(new InternetAddress("tony@example.com"), verb.getAddress());
        assertEquals(12345, verb.getSize());
        assertEquals("8BITMIME", verb.getBody());

        verb = new MailVerb(new Verb("MAIL FROM: <tony@example.com> RET=HDRS ENVID=QQ314159 BODY=BINARYMIME"));
        assertEquals(new InternetAddress("tony@example.com"), verb.getAddress());
        assertEquals("HDRS", verb.getRet());
        assertEquals("QQ314159", verb.getEnvId());
        assertEquals("BINARYMIME", verb.getBody());

        verb = new MailVerb(new Verb("RCPT TO:<tony@example.com> NOTIFY=FAILURE ORCPT=rfc822;happy@example.com"));
        assertEquals(new InternetAddress("tony@example.com"), verb.getAddress());
        assertArrayEquals(new String[]{"FAILURE"}, verb.getNotify());
        assertEquals(new InternetAddress("happy@example.com"), verb.getORcpt());

        verb = new MailVerb(new Verb("RCPT TO: <tony@example.com> NOTIFY=SUCCESS,FAILURE"));
        assertEquals(new InternetAddress("tony@example.com"), verb.getAddress());
        assertArrayEquals(new String[]{"SUCCESS", "FAILURE"}, verb.getNotify());
    }

    @Test
    void auth() {
        AuthVerb verb;
        verb = new AuthVerb(new Verb("AUTH PLAIN dGVzdAB0ZXN0ADEyMzQ="));
        assertEquals("test", verb.getCid());
        assertEquals("test", verb.getUsername());
        assertEquals("1234", verb.getPassword());

        verb = new AuthVerb(new Verb("AUTH LOGIN"));
        assertEquals("", verb.getUsername());
        assertEquals("", verb.getPassword());

        verb = new AuthVerb(new Verb("AUTH LOGIN"));
        assertEquals("LOGIN", verb.getType());
        verb.setUsername("test");
        verb.setPassword("1234");
        assertEquals("LOGIN", verb.getType());
        assertEquals("test", verb.getUsername());
        assertEquals("1234", verb.getPassword());

        verb = new AuthVerb(new Verb("AUTH LOGIN dGVzdA=="));
        verb.setPassword("1234");
        assertEquals("LOGIN", verb.getType());
        assertEquals("test", verb.getUsername());
        assertEquals("1234", verb.getPassword());
    }

    @Test
    void bdat() {
        BdatVerb verb;
        verb = new BdatVerb(new Verb("BDAT"));
        assertEquals(0, verb.getSize());

        verb = new BdatVerb(new Verb("BDAT 0"));
        assertEquals(0, verb.getSize());

        verb = new BdatVerb(new Verb("BDAT 1234"));
        assertEquals(1234, verb.getSize());
        assertFalse(verb.isLast());

        verb = new BdatVerb(new Verb("BDAT 123456798 LAST"));
        assertEquals(123456798, verb.getSize());
        assertTrue(verb.isLast());
    }

    @Test
    void error() {
        BdatVerb verb;
        verb = new BdatVerb(new Verb("NOT"));
        assertTrue(verb.isError());

        verb = new BdatVerb(new Verb("NOTS"));
        assertFalse(verb.isError());
    }
}

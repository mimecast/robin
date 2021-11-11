package com.mimecast.robin.smtp.io;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.MessageEnvelope;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MagicInputStreamTest {

    private static MagicInputStream magic;
    private static MessageEnvelope envelope;

    @BeforeAll
    static void before() throws FileNotFoundException, ConfigurationException {
        Foundation.init("src/test/resources/");

        envelope = new MessageEnvelope();
        envelope.setFile("src/test/resources/lipsum.eml");
        envelope.setMail("rocket@example.com");
        envelope.setRcpt("groot@example.com");
        envelope.setMailEjf("peter@example.com");
        envelope.setRcptEjf("gamora@example.com");

        FileInputStream file = new FileInputStream(envelope.getFile());
        magic = new MagicInputStream(file, envelope);
    }

    @Test
    void readLine() throws IOException {
        List<String> lines = new ArrayList<>();

        byte[] bytes;
        while ((bytes = magic.readLine()) != null) {
            lines.add(new String(bytes));
        }

        assertEquals(65, lines.size());
        assertEquals("From: <rocket@example.com>", lines.get(1).trim());
        assertEquals("To: <groot@example.com>", lines.get(2).trim());
        assertEquals("Subject: Lipsum", lines.get(5).trim());
        assertEquals("Integer at finibus orci.", lines.get(15).trim());
        assertEquals("Content-Transfer-Encoding: 8bit", lines.get(30).trim());
        assertEquals("--MCBoundary11505141140170031--", lines.get(64).trim());
    }

    @Test
    void doMagic() {
        assertEquals("{$randch", new String(magic.doMagic("{$randch".getBytes())));
        assertEquals("{$randno", new String(magic.doMagic("{$randno".getBytes())));

        assertEquals(20, magic.doMagic("{$randch}".getBytes()).length);
        assertEquals(57, magic.doMagic("{$randch57}".getBytes()).length);

        assertTrue(Integer.parseInt(new String(magic.doMagic("{$randno}".getBytes()))) <= 20);
        assertTrue(Integer.parseInt(new String(magic.doMagic("{$randno75}".getBytes()))) <= 75);

        MagicInputStream nulll = new MagicInputStream(new BufferedInputStream(new ByteArrayInputStream("".getBytes())), null);
        assertEquals("groot", new String(nulll.doMagic("groot".getBytes())));
    }

    @Test
    void doSimpleMagic() {
        assertEquals(envelope.getMessageId(), magic.doSimpleMagic("{$msgid}", "{$msgid}"));
        assertEquals(envelope.getDate(), magic.doSimpleMagic("{$date}", "{$date}"));
        assertEquals(envelope.getYymd(), magic.doSimpleMagic("{$yymd}", "{$yymd}"));
        assertEquals(envelope.getMailFrom(), magic.doSimpleMagic("{$mailfrom}", "{$mailfrom}"));
        assertEquals(envelope.getRcptTo(), magic.doSimpleMagic("{$rcptto}", "{$rcptto}"));
        assertEquals(envelope.getMailEjfFrom(), magic.doSimpleMagic("{$mailejffrom}", "{$mailejffrom}"));
        assertEquals(envelope.getRcptEjfTo(), magic.doSimpleMagic("{$rcptejfto}", "{$rcptejfto}"));
    }

    @Test
    void getReplacement() {
        assertEquals(envelope.getMessageId(), magic.getReplacement("{$msgid}"));
        assertEquals(envelope.getDate(), magic.getReplacement("{$date}"));
        assertEquals(envelope.getYymd(), magic.getReplacement("{$yymd}"));
        assertEquals(envelope.getMailFrom(), magic.getReplacement("{$mailfrom}"));
        assertEquals(envelope.getRcptTo(), magic.getReplacement("{$rcptto}"));
        assertEquals(envelope.getMailEjfFrom(), magic.getReplacement("{$mailejffrom}"));
        assertEquals(envelope.getRcptEjfTo(), magic.getReplacement("{$rcptejfto}"));
        assertEquals("", magic.getReplacement("rocket"));

        MagicInputStream empty = new MagicInputStream(new BufferedInputStream(new ByteArrayInputStream("".getBytes())), new MessageEnvelope());
        assertEquals("", empty.getReplacement(""));

        MagicInputStream nulll = new MagicInputStream(new BufferedInputStream(new ByteArrayInputStream("".getBytes())), null);
        assertEquals("", nulll.getReplacement(""));
    }

    @Test
    void randCh() {
        assertEquals("tony", magic.randCh("tony"));
        assertEquals(20, magic.randCh("{$randch}").length());
        assertEquals(57, magic.randCh("{$randch57}").length());
    }

    @Test
    void randNo() {
        assertEquals("pepper", magic.randNo("pepper"));
        assertTrue(Integer.parseInt(magic.randNo("{$randno}")) <= 20);
        assertTrue(Integer.parseInt(magic.randNo("{$randno75}")) <= 75);
    }
}

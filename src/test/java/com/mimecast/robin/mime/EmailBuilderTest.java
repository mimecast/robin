package com.mimecast.robin.mime;

import com.mimecast.robin.mime.parts.FileMimePart;
import com.mimecast.robin.mime.parts.TextMimePart;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.util.StreamUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailBuilderTest {

    @Test
    void defaultHeaders() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        EmailBuilder emailBuilder = new EmailBuilder(new MessageEnvelope())
                .writeTo(outputStream);

        Map<Integer, String> lines = StreamUtils.parseLines(outputStream);
        assertEquals("MIME-Version: 1.0\r\n", lines.get(1));
        assertTrue(lines.get(2).startsWith("Date: "));
        assertTrue(lines.get(3).startsWith("Message-ID: "));
        assertTrue(lines.get(4).startsWith("Subject: Robin "));
        assertEquals("From: <>\r\n", lines.get(5));
        assertEquals("To: <>\r\n", lines.get(6));
    }

    @Test
    void singlePart() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        EmailBuilder emailBuilder = new EmailBuilder(new MessageEnvelope())
                .addHeader("Subject", "Robin wrote")
                .addHeader("To", "Lady Robin <lady.robin@example.com>")
                .addHeader("From", "Sir Robin <sir.robin@example.com>")

                .addPart(new TextMimePart(("Mon chéri,\r\n" +
                        "\r\n" +
                        "Please review this lovely blog post I have written about myself.\r\n" +
                        "Huge ego, right?\r\n" +
                        "\r\n" +
                        "Kisses,\r\n" +
                        "Your Robin.").getBytes())
                        .addHeader("Content-Type", "text/plain; charset=\"ISO-8859-1\"")
                )
                .writeTo(outputStream);

        Map<Integer, String> lines = StreamUtils.parseLines(outputStream);
        assertEquals("MIME-Version: 1.0\r\n", lines.get(1));
        assertEquals("Subject: Robin wrote\r\n", lines.get(2));
        assertEquals("To: Lady Robin <lady.robin@example.com>\r\n", lines.get(3));
        assertEquals("From: Sir Robin <sir.robin@example.com>\r\n", lines.get(4));
        assertTrue(lines.get(5).startsWith("Date: "));
        assertTrue(lines.get(6).startsWith("Message-ID: "));
        assertEquals("Content-Type: text/plain; charset=\"ISO-8859-1\"\r\n", lines.get(7));
        assertEquals("\r\n", lines.get(8));
        assertEquals("Mon chéri,\r\n", lines.get(9));
        assertEquals("Your Robin.\r\n", lines.get(15));
    }

    @Test
    void multiPart() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        EmailBuilder emailBuilder = new EmailBuilder(new MessageEnvelope())
                .addHeader("Subject", "Robin wrote")
                .addHeader("To", "Sir Robin <sir.robin@example.com>")
                .addHeader("From", "Lady Robin <lady.robin@example.com>")

                .addPart(new TextMimePart(("Mon chéri,\r\n" +
                        "\r\n" +
                        "Please review this lovely blog post i have written about myself.\r\n" +
                        "Huge ego, right?\r\n" +
                        "\r\n" +
                        "Kisses,\r\n" +
                        "Your Robin.").getBytes())
                        .addHeader("Content-Type", "text/plain; charset=\"ISO-8859-1\"")
                        .addHeader("Content-Transfer-Encoding", "quoted-printable")
                )

                .addPart(
                        new FileMimePart("src/test/resources/mime/selfie.jpg")
                                .addHeader("Content-Transfer-Encoding", "base64")
                                .addHeader("Content-ID", "<selfie@example.com>")
                )

                .addPart(
                        new FileMimePart("src/test/resources/mime/robin.article.pdf")
                                .addHeader("Content-Transfer-Encoding", "base64")
                )
                .writeTo(outputStream);

        Map<Integer, String> lines = StreamUtils.parseLines(outputStream);
        assertEquals("MIME-Version: 1.0\r\n", lines.get(1));
        assertEquals("Subject: Robin wrote\r\n", lines.get(2));
        assertEquals("To: Sir Robin <sir.robin@example.com>\r\n", lines.get(3));
        assertEquals("From: Lady Robin <lady.robin@example.com>\r\n", lines.get(4));
        assertTrue(lines.get(5).startsWith("Date: "));
        assertTrue(lines.get(6).startsWith("Message-ID: "));
        assertEquals("Content-Type: multipart/mixed; boundary=\"robinMixed\"\r\n", lines.get(7));
        assertEquals("\r\n", lines.get(8));
        assertEquals("--robinMixed\r\n", lines.get(9));
        assertEquals("Content-Type: multipart/related; boundary=\"robinRelated\"\r\n", lines.get(10));
        assertEquals("\r\n", lines.get(11));
        assertEquals("--robinRelated\r\n", lines.get(12));
        assertEquals("Content-Type: multipart/alternative; boundary=\"robinAlternative\"\r\n", lines.get(13));
        assertEquals("\r\n", lines.get(14));
        assertEquals("--robinAlternative\r\n", lines.get(15));
        assertEquals("Content-Type: text/plain; charset=\"ISO-8859-1\"\r\n", lines.get(16));
        assertEquals("Content-Transfer-Encoding: quoted-printable\r\n", lines.get(17));
        assertEquals("\r\n", lines.get(18));
        assertEquals("Mon ch=C3=A9ri,\r\n", lines.get(19));
        assertEquals("Your Robin.\r\n", lines.get(25));
        assertEquals("--robinAlternative--\r\n", lines.get(26));
        assertEquals("--robinRelated\r\n", lines.get(27));
        assertEquals("Content-Transfer-Encoding: base64\r\n", lines.get(28));
        assertEquals("Content-ID: <selfie@example.com>\r\n", lines.get(29));
        assertEquals("Content-Type: application/octet-stream\r\n", lines.get(30));
        assertEquals("\r\n", lines.get(31));
        assertEquals("/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAoHBwgHBgoICAgLCgoLDhgQDg0NDh0VFhEYIx8lJCIf\r\n", lines.get(32));
        assertEquals("30HPqs0RAREQEREBERAREQEREBERAREQEREBERAREQEREBERAREQEREBERAREQf/2Q==\r\n", lines.get(83));
        assertEquals("--robinRelated--\r\n", lines.get(84));
        assertEquals("--robinMixed\r\n", lines.get(85));
        assertEquals("Content-Transfer-Encoding: base64\r\n", lines.get(86));
        assertEquals("Content-Type: application/octet-stream\r\n", lines.get(87));
        assertEquals("\r\n", lines.get(88));
        assertEquals("JVBERi0xLjQKJdPr6eEKMSAwIG9iago8PC9DcmVhdG9yIChNb3ppbGxhLzUuMCBcKE1hY2ludG9z\r\n", lines.get(89));
    }
}

package com.mimecast.robin.mime;

import com.mimecast.robin.mime.parts.MimePart;
import com.mimecast.robin.mime.parts.TextMimePart;
import com.mimecast.robin.smtp.io.LineInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class EmailParserTest {

    static final String dir = "src/test/resources/cases/sources/";

    @Test
    @DisplayName("Parse headers of email finds correct headers")
    void headers() throws IOException {
        String mime = "MIME-Version: 1.0\r\n" +
                "From: Lady Robin <lady.robin@example.com>\r\n" +
                "To: Sir Robin <sir.robin@example.com>\r\n" +
                "Date: Thu, 28 Jan 2021 20:27:09 +0000\r\n" +
                "Message-ID: <twoRobinsMakeAFamily@example.com>\r\n" +
                "Subject: Robin likes\r\n" +
                "Content-Type: text/plain; charset=\"ISO-8859-1\",\r\n\tname=robin.txt,\r\n\tlanguage='en_UK';\r\n" +
                "Content-Disposition: inline charset='ISO-8859-1'\r\n\tfilename=robin.txt;\r\n\tlanguage=en_UK,";
        EmailParser parser = new EmailParser(new LineInputStream(new ByteArrayInputStream(mime.getBytes())))
                .parse(true);

        assertEquals("1.0", parser.getHeaders().get("MIME-Version").get().getValue());
        assertEquals("Lady Robin <lady.robin@example.com>", parser.getHeaders().get("From").get().getValue());
        assertEquals("Sir Robin <sir.robin@example.com>", parser.getHeaders().get("To").get().getValue());
        assertEquals("Thu, 28 Jan 2021 20:27:09 +0000", parser.getHeaders().get("Date").get().getValue());
        assertEquals("<twoRobinsMakeAFamily@example.com>", parser.getHeaders().get("Message-ID").get().getValue());
        assertEquals("Robin likes", parser.getHeaders().get("Subject").get().getValue());

        assertEquals("text/plain; charset=\"ISO-8859-1\",\r\n\tname=robin.txt,\r\n" +
                "\tlanguage='en_UK';", parser.getHeaders().get("Content-Type").get().getValue());

        assertEquals("ISO-8859-1", parser.getHeaders().get("Content-Type").get().getParameter("charset"));
        assertEquals("robin.txt", parser.getHeaders().get("Content-Type").get().getParameter("name"));
        assertEquals("en_UK", parser.getHeaders().get("Content-Type").get().getParameter("language"));

        assertEquals("inline charset='ISO-8859-1'\r\n\tfilename=robin.txt;\r\n" +
                "\tlanguage=en_UK,", parser.getHeaders().get("Content-Disposition").get().getValue());

        assertEquals("ISO-8859-1", parser.getHeaders().get("Content-Disposition").get().getParameter("charset"));
        assertEquals("robin.txt", parser.getHeaders().get("Content-Disposition").get().getParameter("filename"));
        assertEquals("en_UK", parser.getHeaders().get("Content-Disposition").get().getParameter("language"));
    }

    @Test
    @DisplayName("Parse lipsum.eml gives 2 parts")
    void parseLipsum() throws IOException {
        EmailParser parser = new EmailParser(new LineInputStream(new FileInputStream(dir + "lipsum.eml")));
        parser.parse();

        assertEquals(3, parser.getParts().size(), "Unexpected number of parts");

        assertEquals("uSdGze9aOjGMKP/QLtT9szHfcNV5K9DoaP12xlasxeU=", parser.getParts().get(1).getHash(HashType.SHA_256), "unexpected hash");
        assertEquals(780, parser.getParts().get(1).getSize(), "unexpected file size");

        assertTrue(validateTextPart(parser.getParts(), 1508, HashType.SHA_256, "bksYTbn+IdI8bDjJTjArvxdEXj719WVpDWmj96KfHAU="));

        assertEquals("UTF-8", parser.getParts().get(2).getHeaders().get("Content-type").get().getParameter("charset"), "inexpected charset");
    }

    @SuppressWarnings("SameParameterValue")
    boolean validateTextPart(List<MimePart> parts, long size, HashType hashType, String hashValue) {
        for (MimePart part : parts) {
            if (part instanceof TextMimePart && part.getSize() == size && part.getHash(hashType).equals(hashValue)) {
                return true;
            }
        }

        return false;
    }
}

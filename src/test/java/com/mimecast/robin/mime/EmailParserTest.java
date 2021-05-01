package com.mimecast.robin.mime;

import com.mimecast.robin.mime.headers.MimeHeader;
import com.mimecast.robin.mime.headers.MimeHeaders;
import com.mimecast.robin.mime.parts.FileMimePart;
import com.mimecast.robin.mime.parts.MimePart;
import com.mimecast.robin.mime.parts.MultipartMimePart;
import com.mimecast.robin.mime.parts.TextMimePart;
import com.mimecast.robin.smtp.io.LineInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Test
    @DisplayName("Parse lipsum.attachment.eml with quoted printable")
    void parseLipsumAttachment() throws IOException {
        EmailParser parser = new EmailParser(new LineInputStream(new FileInputStream(dir + "lipsum.attachment.eml")));
        parser.parse();

        assertEquals(4, parser.getParts().size(), "Unexpected number of parts");

        String expectedHtml = "<html>\r\n" +
                "<head>\r\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=us-ascii\">\r\n" +
                "</head>\r\n" +
                "<body style=\"word-wrap: break-word; -webkit-nbsp-mode: space; -webkit-line-break: after-white-space; color: rgb(0, 0, 0); font-size: 14px; font-family: Calibri, sans-serif;\">\r\n" +
                "<div>trest</div>\r\n" +
                "</body>\r\n" +
                "</html>\r\n";

        String html = ((TextMimePart) parser.getParts().get(2)).getContent();
        assertEquals(expectedHtml, html, "HTML does not match the expected");
    }

    @Test
    @DisplayName("Parse attachment.only.eml gives 2 parts")
    void parseAttachmentOnly() throws IOException {
        EmailParser parser = new EmailParser(new LineInputStream(new FileInputStream(dir + "attachment.only.eml")));
        parser.parse();

        assertEquals(1, parser.getParts().size(), "Unexpected number of parts");

        assertTrue(validateFilePart(parser.getParts(), 127585, HashType.SHA_256, "U4BVeJd35cI+IBwTCTUQUtrzA/+vmukl/ov1hZYMCXU=", "lorem.docx"));
    }

    @Test
    @DisplayName("Parse attachment.zero.eml creates 5 parts of the correct type")
    void parseAttachmentZero() throws IOException {
        EmailParser parser = new EmailParser(new LineInputStream(new FileInputStream(dir + "attachment.zero.eml")));
        parser.parse();

        assertEquals(5, parser.getParts().size(), "Unexpected number of parts");

        assertTrue(parser.getParts().get(0) instanceof MultipartMimePart);
        assertTrue(parser.getParts().get(1) instanceof FileMimePart);
        assertTrue(parser.getParts().get(2) instanceof TextMimePart);
        assertTrue(parser.getParts().get(3) instanceof TextMimePart);
        assertTrue(parser.getParts().get(4) instanceof FileMimePart);
    }

    @Test
    @DisplayName("Parse binary.eml with results directory creates 3 files in the results directory")
    void parseBinary() throws IOException {
        EmailParser parser = new EmailParser(new LineInputStream(new FileInputStream(dir + "binary.eml")));
        parser.parse();

        assertEquals(4, parser.getParts().size(), "Unexpected number of parts");
        assertTrue(validateFilePart(parser.getParts(), 186562, HashType.SHA_256, "li7HjHndowBKN2zRPUh3vf38/UlzvMTDS9FizrpVzQg=", "test.pdf"));
    }

    @Test
    @DisplayName("Parse document.eml contains correct doc part with correct hash size and filename")
    void parseDocument() throws IOException {
        EmailParser parser = new EmailParser(new LineInputStream(new FileInputStream(dir + "document.eml")));
        parser.parse();

        assertTrue(validateFilePart(parser.getParts(), 22016, HashType.SHA_256, "RSYGefo7MpGjbwYqat8SO2YLoW4+UFMEvrMDTP5VWt0=", "doc.doc"));
    }

    @Test
    @DisplayName("Parse eml.message.eml finds multipart/mixed,mutlipart/alternative,text,html and message/rfc822")
    void parseRFC822() throws IOException {
        EmailParser parser = new EmailParser(new LineInputStream(new FileInputStream(dir + "eml.message.eml")));
        parser.parse();

        assertEquals(4, parser.getParts().size(), "Unexpected number of parts");

        assertTrue(parser.getParts().get(0) instanceof MultipartMimePart);
        assertTrue(parser.getParts().get(1) instanceof TextMimePart);
        assertTrue(parser.getParts().get(2) instanceof TextMimePart);
        assertTrue(parser.getParts().get(3) instanceof FileMimePart);

        String topContentType = parser.getParts().get(0).getHeader("Content-Type").getValue();
        assertEquals("multipart/mixed; boundary=047d7bb03f60fc85b70519583bdc", topContentType, "unexpected parent Content-Type");
    }

    @Test
    @DisplayName("Parse bounce.delivery.status.eml finds 3 parts")
    void parseBounceDeliveryStatus() throws IOException {
        EmailParser parser = new EmailParser(new LineInputStream(new FileInputStream(dir + "bounce.delivery.status.eml")));
        parser.parse();

        assertEquals(3, parser.getParts().size(), "Unexpected number of parts");

        assertTrue(parser.getParts().get(1) instanceof TextMimePart);

        assertTrue(validateTextPart(parser.getParts(), 107, HashType.SHA_1, "+u2i3IJlM+cuHO2xfIGNCj/nLDA="));
        assertTrue(validateFilePart(parser.getParts(), 288, HashType.MD_5, "uJ0PROO+qtTJMOKgBtBwNA==", "derlivery-status.txt"));

        MimeHeaders headers = new MimeHeaders();
        headers.put(new MimeHeader("Content-Description", "Delivery report"));
        headers.put(new MimeHeader("Content-Type", "message/delivery-status"));
        assertTrue(validatePartMatchesHeaders(parser.getParts(), headers), "Can't find headers");
    }

    @Test
    @DisplayName("iCal")
    void parseIcal() throws IOException {
        EmailParser parser = new EmailParser(new LineInputStream(new FileInputStream(dir + "ical.a.cid.eml")));
        parser.parse();
    }

    @Test
    @DisplayName("Parse iso-8859-1.eml gives 2 parts")
    void parseIso8859dash1() throws IOException {
        EmailParser parser = new EmailParser(new LineInputStream(new FileInputStream(dir + "iso-8859-1.eml")));
        parser.parse();

        assertEquals(1, parser.getParts().size(), "Unexpected number of parts");

        assertEquals("kWQ1Maa+Kfo5FwjzEePVEDwldgl8VvRM3kCkAsPFkVA=", parser.getParts().get(0).getHash(HashType.SHA_256), "Unexpected hash");
        assertEquals(10, parser.getParts().get(0).getSize(), "Unexpected file size");
        assertEquals("iso-8859-1", parser.getParts().get(0).getHeaders().get("Content-type").get().getParameter("charset"), "Unexpected charset");
        assertEquals("Berrangé\r\n", ((TextMimePart) parser.getParts().get(0)).getContent(), "Unexpected charset");
    }

    @Test
    @DisplayName("Parsing using a filescan of sources directry doesn't throw any exceptions")
    void testItAll() throws IOException {
        File fScanDirectory = new File(dir);
        File[] files = fScanDirectory.listFiles();

        if (files != null) {
            for (File f : files) {
                String eml = f.getAbsolutePath();
                if (!eml.endsWith("eml")) {
                    continue;
                }

                EmailParser parser = new EmailParser(new LineInputStream(new FileInputStream(eml)));
                parser.parse();
            }
        }
    }

    boolean validatePartMatchesHeaders(List<MimePart> parts, MimeHeaders headers) {
        for (MimePart part : parts) {
            // Using a set so we know when we've found all the headers avoiding any duplicates!
            Set<String> foundHeaders = new HashSet<>();
            for (MimeHeader hdr : headers.get()) {
                Optional<MimeHeader> item = part.getHeaders().get(hdr.getName());
                if (item.isPresent()) {
                    if (item.get().getValue().equalsIgnoreCase(hdr.getValue())) {
                        foundHeaders.add(hdr.getName());
                    }
                }
            }
            if (foundHeaders.size() == headers.get().size()) {
                return true;
            }
        }
        return false;
    }

    boolean validateTextPart(List<MimePart> parts, long size, HashType hashType, String hashValue) {
        for (MimePart part : parts) {
            if (part instanceof TextMimePart && part.getSize() == size && part.getHash(hashType).equals(hashValue)) {
                return true;
            }
        }

        return false;
    }

    boolean validateFilePart(List<MimePart> parts, long size, HashType hashType, String hashValue, String fileName) {
        for (MimePart part : parts) {
            if (part instanceof FileMimePart && part.getSize() == size && part.getHash(hashType).equals(hashValue)) {

                for (MimeHeader mimeHeader : part.getHeaders().get()) {
                    if (mimeHeader.getParameter("filename") != null) {
                        if (mimeHeader.getParameter("filename").equalsIgnoreCase(fileName)) {
                            return true;
                        }
                    }

                    if (mimeHeader.getParameter("name") != null) {
                        if (mimeHeader.getParameter("name").equalsIgnoreCase(fileName)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}

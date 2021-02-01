package com.mimecast.robin.mime;

import com.mimecast.robin.smtp.io.LineInputStream;
import org.junit.jupiter.api.Test;

import javax.mail.internet.ParseException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailParserTest {

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    void headers() throws IOException, ParseException {
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
}
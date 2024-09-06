package cases;

import com.mimecast.robin.assertion.Assert;
import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.config.assertion.AssertConfig;
import com.mimecast.robin.config.client.RouteConfig;
import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.mime.EmailBuilder;
import com.mimecast.robin.mime.parts.FileMimePart;
import com.mimecast.robin.mime.parts.TextMimePart;
import com.mimecast.robin.smtp.EmailDelivery;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.session.Session;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ExampleProgramatic {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("cfg/");
    }

    /**
     * Programmatic example of a case.
     */
    @Test
    void programmaticUsage() throws AssertException, IOException {
        // Temp file for MIME generation with auto delete.
        Path path = Files.createTempFile("temp-", ".tmp");
        try (Closeable ignored = () -> Files.delete(path)) {

            // Build MIME.
            new EmailBuilder(new Session(), new MessageEnvelope())
                    .addHeader("Subject", "Robin wrote")
                    .addHeader("To", "Sir Robin <robin@example.com>")
                    .addHeader("From", "Lady Robin <lady@example.com>")
                    .addHeader("X-Robin-Filename", "the.robins.eml")

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
                            new FileMimePart("src/test/resources/mime/robin.article.pdf")
                                    .addHeader("Content-Type", "application/pdf; name=\"article.pdf\"")
                                    .addHeader("Content-Disposition", "attachment; filename=\"article.pdf\"")
                                    .addHeader("Content-Transfer-Encoding", "base64")
                    )
                    .writeTo(new FileOutputStream(path.toFile()));

            // Session configuration.
            Session session = new Session()
                    // Uses MimecastSession for all capabilities so use Mimecast setters here.
                    .setRetry(3)
                    .setDelay(5)
                    .setEhlo("dynamic.test");

            RouteConfig route = Config.getClient().getRoute("local");
            session.setMx(route.getMx())
                    .setPort(route.getPort())
                    .setAuth(route.isAuth())
                    .setUsername(route.getUser())
                    .setPassword(route.getPass());

            // Email envelope.
            MessageEnvelope envelope = new MessageEnvelope();
            envelope.setMail("robin@example.com");
            envelope.getRcpts().add("lady@example.com");
            envelope.setStream(new FileInputStream(path.toFile())); // Email temp file is streamed here.

            // Envelope assertions.
            List<List<String>> smtpAssertions = new ArrayList<>();
            smtpAssertions.add(Arrays.asList("MAIL", "250 Sender OK"));
            smtpAssertions.add(Arrays.asList("RCPT", "250 Recipient OK"));
            smtpAssertions.add(Arrays.asList("DATA", "^250"));
            smtpAssertions.add(Arrays.asList("DATA", "Received OK"));

            Map<String, Object> map = new HashMap<>();
            map.put("smtp", smtpAssertions);

            envelope.setAssertions(new AssertConfig(map));
            session.addEnvelope(envelope);

            // Send & assert.
            EmailDelivery delivery = new EmailDelivery(session).send();
            new Assert(delivery.getConnection()).run();
        }
    }
}

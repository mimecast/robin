package cases;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.main.Client;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ExampleSend {

    /**
     * JSON example of a basic test that sends an eml file.
     */
    @Test
    void plainTextEml() throws AssertException, IOException {
        new Client()
                .send("src/test/resources/cases/config/lipsum.json5");
    }

    /**
     * JSON example of a basic test that sends an UTF-8 eml file.
     */
    @Test
    void plainTextUtf8Eml() throws AssertException, IOException {
        new Client()
                .send("src/test/resources/cases/config/pangrams.json5");
    }

    /**
     * JSON example of a test with built email from MIME.
     */
    @Test
    void dynamicMime() throws AssertException, IOException {
        new Client()
                .send("src/test/resources/cases/config/dynamic/dynamic.json5");
    }

    /**
     * JSON example of a test with built email from MIME with randomly generated PDF.
     */
    @Test
    void dynamicPdf() throws AssertException, IOException {
        new Client()
                .send("src/test/resources/cases/config/dynamic/dynamic.pdf.json5");
    }

    /**
     * JSON example of a basic test that uses XCLIENT extension.
     */
    @Test
    void xclient() throws AssertException, IOException {
        new Client()
                .send("src/test/resources/cases/config/xclient.json5");
    }
}

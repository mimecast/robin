package cases;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.main.Client;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;

/**
 * Sample test cases.
 */
class Sample {

    /**
     * Sends a "Lorem ipsum" paragraph email to client.json defaults with TLS 1.2.
     * <p>It runs STARTTLS extension with TLS 1.2 and a specific set of cyphers if advertised.
     *
     * @throws AssertException Case assertion failed.
     * @throws IOException     Communication issues.
     */
    @Test
    @SuppressWarnings("java:S2699")
    void lipsum() throws AssertException, IOException, ConfigurationException {
        new Client("src/main/resources/")
                .send("src/test/resources/cases/config/lipsum.json");
    }

    /**
     * Sends a pangrams UTF-8 email to client.json defaults with CHUNKING.
     * <p>It used CHUNKING extension if advertised.
     * <p>Sends 2048 byte chunks except last.
     * <p>Includes the BDAT command in the first chunk.
     *
     * @throws AssertException Case assertion failed.
     * @throws IOException     Communication issues.
     */
    @Test
    @SuppressWarnings("java:S2699")
    void pangrams() throws AssertException, IOException, ConfigurationException {
        new Client("src/main/resources/")
                .send("src/test/resources/cases/config/pangrams.json");
    }

    /**
     * Make an HTTP POST request with a JSON.
     *
     * @throws AssertException Case assertion failed.
     * @throws IOException     Communication issues.
     */
    @Test
    @SuppressWarnings("java:S2699")
    void postJson() throws AssertException, IOException, ConfigurationException {
        new Client("src/main/resources/")
                .send("src/test/resources/cases/config/post-json.json");
    }

    /**
     * Make an HTTP POST request with files upload.
     *
     * @throws AssertException Case assertion failed.
     * @throws IOException     Communication issues.
     */
    @Test
    @SuppressWarnings("java:S2699")
    void postFilesAndParams() throws AssertException, IOException, ConfigurationException {
        new Client("src/main/resources/")
                .send("src/test/resources/cases/config/post-files.json");
    }
}

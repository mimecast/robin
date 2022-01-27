package cases;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.main.RequestClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ExampleHttp {

    /**
     * JSON example of a test with a files POST.
     * <p>Navigate to https://requestcatcher.com to debug.
     */
    @Test
    void files() throws AssertException, IOException {
        new RequestClient()
                .request("src/test/resources/cases/config/request/post-files.json5");
    }

    /**
     * JSON example of a test with a JSON POST.
     * <p>Navigate to https://requestcatcher.com to debug.
     */
    @Test
    void json() throws AssertException, IOException {
        new RequestClient()
                .request("src/test/resources/cases/config/request/post-json.json5");
    }
}

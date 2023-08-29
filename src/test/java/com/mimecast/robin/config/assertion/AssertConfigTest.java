package com.mimecast.robin.config.assertion;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AssertConfigTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void smtp() {
        String json =
                "{\n" +
                "  \"smtp\": [\n" +
                "    [\"MAIL\", \"250 Sender OK\"],\n" +
                "    [\"RCPT\", \"250 Recipient OK\"],\n" +
                "    [\"DATA\", \"^250\"],\n" +
                "    [\"DATA\", \"Received OK$\"]\n" +
                "  ]\n" +
                "}";

        Map<String, Object> map = new Gson().fromJson(json, new TypeToken<HashMap<String, Object>>() {}.getType());
        AssertConfig assertConfig = new AssertConfig(map);

        assertEquals("MAIL", assertConfig.getProtocol().get(0).get(0));
        assertEquals("250 Sender OK", assertConfig.getProtocol().get(0).get(1));
        assertEquals("RCPT", assertConfig.getProtocol().get(1).get(0));
        assertEquals("250 Recipient OK", assertConfig.getProtocol().get(1).get(1));
        assertEquals("DATA", assertConfig.getProtocol().get(2).get(0));
        assertEquals("^250", assertConfig.getProtocol().get(2).get(1));
        assertEquals("DATA", assertConfig.getProtocol().get(3).get(0));
        assertEquals("Received OK$", assertConfig.getProtocol().get(3).get(1));
    }
}

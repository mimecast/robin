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

class AssertMtaConfigTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void mta() {
        String json =
                "{\n" +
                "  \"mta\": {\n" +
                "    \"delay\": 30,\n" +
                "    \"retry\": 3,\n" +
                "    \"match\": [\n" +
                "      [\"MTAEMAILEXPLODE\", \"Skel=Aph-\"],\n" +
                "      [\"MTASPAMRESULT\", \"Act=Acc\"]\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        Map<String, Object> map = new Gson().fromJson(json, new TypeToken<HashMap<String, Object>>() {}.getType());
        AssertConfig assertConfig = new AssertConfig(map);

        assertEquals(30, assertConfig.getMta().getDelay());
        assertEquals(3, assertConfig.getMta().getRetry());
        assertEquals("MTAEMAILEXPLODE", assertConfig.getMta().getMatch().get(0).get(0));
        assertEquals("Skel=Aph-", assertConfig.getMta().getMatch().get(0).get(1));
        assertEquals("MTASPAMRESULT", assertConfig.getMta().getMatch().get(1).get(0));
        assertEquals("Act=Acc", assertConfig.getMta().getMatch().get(1).get(1));
    }
}

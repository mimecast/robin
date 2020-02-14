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
                "    \"wait\": 5,\n" +
                "    \"delay\": 30,\n" +
                "    \"retry\": 3,\n" +
                "    \"verify\": [\"MTAOUTSUMMARY\"],\n" +
                "    \"match\": [\n" +
                "      [\"MTAEMAILEXPLODE\", \"Skel=Aph-\"],\n" +
                "      [\"MTASPAMRESULT\", \"Act=Acc\"]\n" +
                "    ],\n" +
                "    \"refuse\": [\n" +
                "      [\"java.lang.NullPointerException\"]\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        Map<String, Object> map = new Gson().fromJson(json, new TypeToken<HashMap<String, Object>>() {}.getType());
        AssertMtaConfig assertMtaConfig = new AssertConfig(map).getMta();

        assertEquals(5, assertMtaConfig.getWait());
        assertEquals(30, assertMtaConfig.getDelay());
        assertEquals(3, assertMtaConfig.getRetry());
        assertEquals("MTAOUTSUMMARY", assertMtaConfig.getVerify().get(0));
        assertEquals("MTAEMAILEXPLODE", assertMtaConfig.getMatch().get(0).get(0));
        assertEquals("Skel=Aph-", assertMtaConfig.getMatch().get(0).get(1));
        assertEquals("MTASPAMRESULT", assertMtaConfig.getMatch().get(1).get(0));
        assertEquals("Act=Acc", assertMtaConfig.getMatch().get(1).get(1));
        assertEquals("java.lang.NullPointerException", assertMtaConfig.getRefuse().get(0).get(0));
    }
}

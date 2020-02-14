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
    void json() {
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

    @Test
    void integerMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("wait", 1);
        map.put("delay", 5);
        map.put("retry", 1);

        AssertMtaConfig assertMtaConfig = new AssertMtaConfig(map);
        assertEquals(2, assertMtaConfig.getWait()); // Minimum is 2
        assertEquals(5, assertMtaConfig.getDelay());
        assertEquals(1, assertMtaConfig.getRetry());
    }

    @Test
    void doubleMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("wait", 3D);
        map.put("delay", 5D);
        map.put("retry", 3D);

        AssertMtaConfig assertMtaConfig = new AssertMtaConfig(map);
        assertEquals(3, assertMtaConfig.getWait());
        assertEquals(5, assertMtaConfig.getDelay());
        assertEquals(3, assertMtaConfig.getRetry());
    }

    @Test
    void shortMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("wait", (short) 30);
        map.put("delay", (short) 15);
        map.put("retry", (short) 30);

        AssertMtaConfig assertMtaConfig = new AssertMtaConfig(map);
        assertEquals(30, assertMtaConfig.getWait());
        assertEquals(15, assertMtaConfig.getDelay());
        assertEquals(30, assertMtaConfig.getRetry());
    }

    @Test
    void longMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("wait", 100L);
        map.put("delay", 500L);
        map.put("retry", 100L);

        AssertMtaConfig assertMtaConfig = new AssertMtaConfig(map);
        assertEquals(100, assertMtaConfig.getWait());
        assertEquals(500, assertMtaConfig.getDelay());
        assertEquals(100, assertMtaConfig.getRetry());
    }

    @Test
    void stringMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("wait", "1");
        map.put("delay", "5");
        map.put("retry", "1");

        AssertMtaConfig assertMtaConfig = new AssertMtaConfig(map);
        assertEquals(2, assertMtaConfig.getWait()); // Minimum is 2
        assertEquals(5, assertMtaConfig.getDelay());
        assertEquals(1, assertMtaConfig.getRetry());
    }
}

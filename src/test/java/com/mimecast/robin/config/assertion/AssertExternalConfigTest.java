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

class AssertExternalConfigTest {

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
        AssertExternalConfig assertExternalConfig = new AssertExternalConfig(new AssertConfig(map).getMapProperty("mta"));

        assertEquals(5, assertExternalConfig.getWait());
        assertEquals(30, assertExternalConfig.getDelay());
        assertEquals(3, assertExternalConfig.getRetry());
        assertEquals("MTAOUTSUMMARY", assertExternalConfig.getVerify().get(0));
        assertEquals("MTAEMAILEXPLODE", assertExternalConfig.getMatch().get(0).get(0));
        assertEquals("Skel=Aph-", assertExternalConfig.getMatch().get(0).get(1));
        assertEquals("MTASPAMRESULT", assertExternalConfig.getMatch().get(1).get(0));
        assertEquals("Act=Acc", assertExternalConfig.getMatch().get(1).get(1));
        assertEquals("java.lang.NullPointerException", assertExternalConfig.getRefuse().get(0).get(0));
    }

    @Test
    void integerMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("wait", 1);
        map.put("delay", 5);
        map.put("retry", 1);

        AssertExternalConfig assertExternalConfig = new AssertExternalConfig(map);
        assertEquals(2, assertExternalConfig.getWait()); // Minimum is 2
        assertEquals(5, assertExternalConfig.getDelay());
        assertEquals(1, assertExternalConfig.getRetry());
    }

    @Test
    void doubleMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("wait", 3D);
        map.put("delay", 5D);
        map.put("retry", 3D);

        AssertExternalConfig assertExternalConfig = new AssertExternalConfig(map);
        assertEquals(3, assertExternalConfig.getWait());
        assertEquals(5, assertExternalConfig.getDelay());
        assertEquals(3, assertExternalConfig.getRetry());
    }

    @Test
    void shortMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("wait", (short) 30);
        map.put("delay", (short) 15);
        map.put("retry", (short) 30);

        AssertExternalConfig assertExternalConfig = new AssertExternalConfig(map);
        assertEquals(30, assertExternalConfig.getWait());
        assertEquals(15, assertExternalConfig.getDelay());
        assertEquals(30, assertExternalConfig.getRetry());
    }

    @Test
    void longMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("wait", 100L);
        map.put("delay", 500L);
        map.put("retry", 100L);

        AssertExternalConfig assertExternalConfig = new AssertExternalConfig(map);
        assertEquals(100, assertExternalConfig.getWait());
        assertEquals(500, assertExternalConfig.getDelay());
        assertEquals(100, assertExternalConfig.getRetry());
    }

    @Test
    void stringMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("wait", "1");
        map.put("delay", "5");
        map.put("retry", "1");

        AssertExternalConfig assertExternalConfig = new AssertExternalConfig(map);
        assertEquals(2, assertExternalConfig.getWait()); // Minimum is 2
        assertEquals(5, assertExternalConfig.getDelay());
        assertEquals(1, assertExternalConfig.getRetry());
    }
}

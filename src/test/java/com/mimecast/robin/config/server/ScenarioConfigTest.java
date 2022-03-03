package com.mimecast.robin.config.server;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ScenarioConfigTest {

    private static ScenarioConfig scenarioConfig;

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");

        scenarioConfig = Config.getServer().getScenarios().get("reject.com");
    }

    @Test
    void getEhlo() {
        assertEquals("501 Not talking to you", scenarioConfig.getEhlo());
    }

    @Test
    void getMail() {
        assertEquals("451 I'm not listening to you", scenarioConfig.getMail());
    }

    @Test
    void getStartTls() {
        assertEquals("TLSv1.0", scenarioConfig.getStarTls().getListProperty("protocols").get(0));
        assertEquals("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", scenarioConfig.getStarTls().getListProperty("ciphers").get(0));
        assertEquals("220 You will fail", scenarioConfig.getStarTls().getStringProperty("response"));
    }

    @Test
    void getRcpt() {
        assertFalse(scenarioConfig.getRcpt().isEmpty());
        assertEquals("ultron@reject\\.com", scenarioConfig.getRcpt().get(0).get("value"));
        assertEquals("501 Heart not found", scenarioConfig.getRcpt().get(0).get("response"));
    }

    @Test
    void getData() {
        assertEquals("554 Your data is corrupted", scenarioConfig.getData());
    }
}

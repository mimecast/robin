package com.mimecast.robin.assertion;

import com.mimecast.robin.assertion.client.ExternalClientMock;
import com.mimecast.robin.config.assertion.AssertExternalConfig;
import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.transaction.EnvelopeTransactionList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AssertExternalTest {

    private static final EnvelopeTransactionList envelopeTransactionList = new EnvelopeTransactionList();

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");

        envelopeTransactionList.addTransaction("MAIL", "250 Sender OK [7qGJZ4oRNkWJPsu_7ug1nw.localhost]", false);
        envelopeTransactionList.addTransaction("RCPT", "250 Recipient OK", false);
        envelopeTransactionList.addTransaction("DATA", "250 Received OK", false);
    }

    @Test
    @SuppressWarnings("java:S2699")
    void assertion() throws AssertException {
        List<List<String>> matches = new ArrayList<>();
        matches.add(Arrays.asList("MTAAUTH", "Pass=false"));
        matches.add(Arrays.asList("MTACONNSUMMARY", "IP=8.8.8.8"));

        Map<String, Object> map = new HashMap<>();
        map.put("delay", 5);
        map.put("retry", 1);
        map.put("match", matches);

        new AssertExternal(new ExternalClientMock(), new AssertExternalConfig(map));
    }

    @Test
    void assertionMissingPattern() {
        assertThrows(AssertException.class, () -> {
            List<List<String>> matches = new ArrayList<>();
            matches.add(Arrays.asList("MTAAUTH", "Pass=false"));
            matches.add(Arrays.asList("MTANOTREAL", "Param=false"));

            Map<String, Object> map = new HashMap<>();
            map.put("delay", 30);
            map.put("retry", 3);
            map.put("match", matches);

            new AssertExternal(new ExternalClientMock(), new AssertExternalConfig(map));
        });
    }
}

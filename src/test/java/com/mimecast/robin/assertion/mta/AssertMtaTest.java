package com.mimecast.robin.assertion.mta;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.assertion.mta.client.LogsClientMock;
import com.mimecast.robin.config.assertion.AssertMtaConfig;
import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.transaction.EnvelopeTransactionList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AssertMtaTest {

    private static final EnvelopeTransactionList envelopeTransactionList = new EnvelopeTransactionList();

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");

        envelopeTransactionList.addTransaction("MAIL", "250 Sender OK [7qGJZ4oRNkWJPsu_7ug1nw.localhost]", false);
        envelopeTransactionList.addTransaction("RCPT", "250 Recipient OK", false);
        envelopeTransactionList.addTransaction("DATA", "250 Received OK", false);
    }

    @Test
    void assertion() throws AssertException {
        List<List<String>> matches = new ArrayList<>();
        matches.add(Arrays.asList("MTAAUTH", "Pass=false"));
        matches.add(Arrays.asList("MTACONNSUMMARY", "IP=8.8.8.8"));

        Map<String, Object> map = new HashMap<>();
        map.put("delay", 5);
        map.put("retry", 1);
        map.put("match", matches);

        AssertMtaConfig assertions = new AssertMtaConfig(map);

        new AssertMta(new LogsClientMock(), assertions, envelopeTransactionList);
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

            AssertMtaConfig assertions = new AssertMtaConfig(map);

            new AssertMta(new LogsClientMock(), assertions, envelopeTransactionList);
        });
    }

    @Test
    @SuppressWarnings("rawtypes")
    void assertionNoUID() {
        assertThrows(AssertException.class, () -> {
            AssertMtaConfig assertions = new AssertMtaConfig(new HashMap());

            EnvelopeTransactionList envelopeTransactionList = new EnvelopeTransactionList();
            envelopeTransactionList.addTransaction("MAIL", "250 Sender OK", false);

            new AssertMta(new LogsClientMock(), assertions, envelopeTransactionList);
        });
    }
}

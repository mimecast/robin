package com.mimecast.robin.smtp.extension.server;

import com.mimecast.robin.config.server.ScenarioConfig;
import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Factories;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.verb.BdatVerb;
import com.mimecast.robin.smtp.verb.Verb;
import com.mimecast.robin.storage.StorageClient;
import org.apache.commons.io.output.CountingOutputStream;

import java.io.IOException;
import java.util.Optional;

/**
 * DATA extension processor.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class ServerData extends ServerProcessor {

    /**
     * CHUNKING advert.
     *
     * @return Advert string.
     */
    @Override
    public String getAdvert() {
        return Config.getServer().isStartTls() ? "CHUNKING" : "";
    }

    /**
     * DATA processor.
     *
     * @param connection Connection instance.
     * @param verb       Verb instance.
     * @return Boolean.
     * @throws IOException Unable to communicate.
     */
    @Override
    public boolean process(Connection connection, Verb verb) throws IOException {
        super.process(connection, verb);

        if (verb.getKey().equals("bdat")) {
            binary();

        } else {
            ascii();
        }

        return true;
    }

    /**
     * ASCII receipt with extended timeout.
     *
     * @throws IOException Unable to communicate.
     */
    private void ascii() throws IOException {
        connection.write("354 Ready and willing");

        StorageClient storageClient = Factories.getStorageClient(connection);
        CountingOutputStream cos = new CountingOutputStream(storageClient.getStream());

        try {
            connection.setTimeout(Connection.EXTENDEDTIMEOUT);
            connection.readMultiline(cos);
            log.debug("Received: {} bytes", cos.getByteCount());
        } finally {
            connection.setTimeout(Connection.DEFAULTTIMEOUT);
        }

        Optional<ScenarioConfig> opt = connection.getScenario();
        if (opt.isPresent() && opt.get().getData() != null) {
            connection.write(opt.get().getData());
        } else {
            storageClient.save();
            connection.write("250 2.0.0 Received OK");
        }
    }

    /**
     * Binary receipt.
     *
     * @throws IOException Unable to communicate.
     */
    private void binary() throws IOException {
        BdatVerb bdatVerb = new BdatVerb(verb);

        if (verb.getCount() == 1) {
            connection.write("501 5.5.4 Invalid arguments");
        } else {
            // Read bytes.
            StorageClient storageClient = Factories.getStorageClient(connection);
            CountingOutputStream cos = new CountingOutputStream(storageClient.getStream());
            binaryRead(bdatVerb, cos);
            log.debug("Received: {} bytes", cos.getByteCount());

            // Scenario response or accept.
            scenarioResponse();

            if (bdatVerb.isLast()) {
                log.debug("Last chunk received.");
            }
        }
    }

    /**
     * Binary read with extended timeout.
     *
     * @param verb Verb instance.
     * @param cos  CountingOutputStream instance.
     * @throws IOException Unable to communicate.
     */
    private void binaryRead(BdatVerb verb, CountingOutputStream cos) throws IOException {
        try {
            connection.setTimeout(Connection.EXTENDEDTIMEOUT);
            connection.readBytes(verb.getSize(), cos);
        } finally {
            connection.setTimeout(Connection.DEFAULTTIMEOUT);
            log.info("<< BYTES {}", cos.getByteCount());
        }
    }

    /**
     * Scenario response.
     *
     * @throws IOException Unable to communicate.
     */
    private void scenarioResponse() throws IOException {
        Optional<ScenarioConfig> opt = connection.getScenario();
        if (opt.isPresent() && opt.get().getData() != null) {
            connection.write(opt.get().getData());
        }

        // Accept all.
        else {
            connection.write("250 2.0.0 Chunk OK");
        }
    }
}

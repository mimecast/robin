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
 */
public class ServerData extends ServerProcessor {

    /**
     * Number of MIME bytes received.
     */
    private long bytesReceived = 0L;

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
        log.debug("Received: {} bytes", bytesReceived);

        return true;
    }

    /**
     * ASCII receipt with extended timeout.
     *
     * @throws IOException Unable to communicate.
     */
    private void ascii() throws IOException {
        if (connection.getSession().getRcpts().isEmpty()) {
            connection.write("554 5.5.1 No valid recipients");
            return;
        } else {
            connection.write("354 Ready and willing");
        }

        StorageClient storageClient = Factories.getStorageClient(connection);

        try (CountingOutputStream cos = new CountingOutputStream(storageClient.getStream())) {
            connection.setTimeout(connection.getSession().getExtendedTimeout());
            connection.readMultiline(cos);
            bytesReceived = cos.getByteCount();

        } finally {
            connection.setTimeout(connection.getSession().getTimeout());
        }

        Optional<ScenarioConfig> opt = connection.getScenario();
        if (opt.isPresent() && opt.get().getData() != null) {
            storageClient.save();
            connection.write(opt.get().getData() + " [" + storageClient.getUID() + "]");
        } else {
            storageClient.save();
            connection.write("250 2.0.0 Received OK [" + storageClient.getUID() + "]");
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
            bytesReceived = cos.getByteCount();

            if (bdatVerb.isLast()) {
                log.debug("Last chunk received.");
                storageClient.save();
            }

            // Scenario response or accept.
            scenarioResponse(storageClient.getUID());
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
            connection.setTimeout(connection.getSession().getExtendedTimeout());
            connection.readBytes(verb.getSize(), cos);

        } finally {
            connection.setTimeout(connection.getSession().getTimeout());
            log.info("<< BYTES {}", cos.getByteCount());
        }
    }

    /**
     * Scenario response.
     *
     * @param uid UID.
     * @throws IOException Unable to communicate.
     */
    private void scenarioResponse(String uid) throws IOException {
        Optional<ScenarioConfig> opt = connection.getScenario();
        if (opt.isPresent() && opt.get().getData() != null) {
            connection.write(opt.get().getData() + " [" + uid + "]");
        }

        // Accept all.
        else {
            connection.write("250 2.0.0 Chunk OK [" + uid + "]");
        }
    }

    /**
     * Gets bytes received.
     *
     * @return Integer.
     */
    public long getBytesReceived() {
        return bytesReceived;
    }
}

package com.mimecast.robin.smtp.io;

import com.mimecast.robin.smtp.MessageEnvelope;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Input stream with data chunking capabilities for SMTP CHUNKING extension.
 *
 * <p>This wraps an input stream and provides chunked writes.
 * <p>By extending MagicInputStream it also provides magic variable replacement.
 * <p>The chunking is configured by provided MessageEnvelope.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class ChunkedInputStream extends MagicInputStream {

    /**
     * Default chunk size.
     */
    private int chunkSize = 2048;

    /**
     * Headers ended boolean.
     */
    private boolean headersEnded = false;

    /**
     * Finished reading boolean.
     */
    private boolean finished = false;

    /**
     * Container for bytes read over the chunk limit.
     * <p>This is because we read lines.
     */
    private final ByteArrayOutputStream rest = new ByteArrayOutputStream();

    /**
     * Constructs a new ChunkedInputStream instance with given MessageEnvelope.
     *
     * @param in       InputStream instance.
     * @param envelope MessageEnvelope instance.
     */
    public ChunkedInputStream(InputStream in, MessageEnvelope envelope) {
        this(in);
        this.envelope = envelope;
        if (envelope.getChunkSize() > 128) {
            chunkSize = envelope.getChunkSize();
        }
    }

    /**
     * Constructs a new ChunkedInputStream instance.
     *
     * @param in InputStream instance.
     */
    private ChunkedInputStream(InputStream in) {
        super(in);
    }

    /**
     * Gets chunk.
     *
     * @return ByteArrayOutputStream chunk.
     * @throws IOException Unable to read.
     */
    public ByteArrayOutputStream getChunk() throws IOException {
        return getChunk(false);
    }

    /**
     * Gets chunk.
     *
     * @param breakHeader Break header boolean.
     * @return ByteArrayOutputStream chunk.
     * @throws IOException Unable to read.
     */
    public ByteArrayOutputStream getChunk(boolean breakHeader) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Variables.
        int size;
        int diff = 0;
        int total = 0;
        byte[] bytes;

        // Pick up leftovers.
        if (rest.size() > 0) {
            out.write(rest.toByteArray());
            total += rest.size();
            rest.reset();
        }

        // Read lines.
        while ((bytes = super.readLine()) != null) {
            size = bytes.length;
            total += size;

            if (total >= chunkSize) {
                diff = total - chunkSize;
            }

            if (diff > 0) {
                out.write(Arrays.copyOfRange(bytes, 0, bytes.length - diff));
                rest.write(Arrays.copyOfRange(bytes, bytes.length - diff, bytes.length));
                return out;
            }

            out.write(bytes);

            if (breakHeader && !headersEnded && !StringUtils.isNotBlank(new String(bytes))) {
                headersEnded = true;
                return out;
            }
        }

        finished = true;
        return out;
    }

    /**
     * Has chunks.
     *
     * @return Boolean.
     */
    public boolean hasChunks() {
        return !finished;
    }
}

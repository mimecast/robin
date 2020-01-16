package com.mimecast.robin.smtp.io;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.MessageEnvelope;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChunkedInputStreamTest {

    private static MessageEnvelope envelope;

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");

        envelope = new MessageEnvelope();
        envelope.setFile("src/test/resources/lipsum.eml");
        envelope.setChunkBdat(true);
    }

    ChunkedInputStream getStream() throws IOException {
        FileInputStream file = new FileInputStream(new File(envelope.getFile()));
        return new ChunkedInputStream(file, envelope);
    }

    @Test
    void getChunk256() throws IOException {
        envelope.setChunkSize(256);

        ChunkedInputStream stream = getStream();
        List<ByteArrayOutputStream> chunks = new ArrayList<>();
        while(stream.hasChunks()) {
            chunks.add(stream.getChunk());
        }

        assertEquals(11, chunks.size());
        for (int i = 0; i < chunks.size() - 1; i++) {
            assertEquals(256, chunks.get(i).size());
        }
        assertEquals(245, chunks.get(10).size());
    }

    @Test
    void getChunkZero() throws IOException {
        envelope.setChunkSize(0); // Defaults to 1400 as size has to be > 0.

        ChunkedInputStream stream = getStream();
        List<ByteArrayOutputStream> chunks = new ArrayList<>();
        while(stream.hasChunks()) {
            chunks.add(stream.getChunk());
        }

        assertEquals(3, chunks.size());
        for (int i = 0; i < chunks.size() - 1; i++) {
            assertEquals(1400, chunks.get(i).size());
        }
        assertEquals(5, chunks.get(2).size());
    }

    @Test
    void getChunkLess() throws IOException {
        envelope.setChunkSize(100); // Defaults to 128 as size is under min viable limit of 128.

        ChunkedInputStream stream = getStream();
        List<ByteArrayOutputStream> chunks = new ArrayList<>();
        while(stream.hasChunks()) {
            chunks.add(stream.getChunk());
        }

        assertEquals(3, chunks.size());
        for (int i = 0; i < chunks.size() - 1; i++) {
            assertEquals(1400, chunks.get(i).size());
        }
        assertEquals(5, chunks.get(2).size());
    }

    @Test
    void getChunkBreak() throws IOException {
        envelope.setChunkSize(512);

        ChunkedInputStream stream = getStream();
        List<ByteArrayOutputStream> chunks = new ArrayList<>();
        while(stream.hasChunks()) {
            chunks.add(stream.getChunk(true));
        }

        assertEquals(6, chunks.size());
        assertEquals(271, chunks.get(0).size());
        assertEquals(512, chunks.get(1).size());
        assertEquals(512, chunks.get(2).size());
        assertEquals(512, chunks.get(3).size());
        assertEquals(512, chunks.get(4).size());
        assertEquals(486, chunks.get(5).size());
    }
}

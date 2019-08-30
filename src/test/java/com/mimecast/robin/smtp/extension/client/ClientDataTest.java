package com.mimecast.robin.smtp.extension.client;

import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.connection.ConnectionMock;
import com.mimecast.robin.smtp.transaction.EnvelopeTransactionList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class ClientDataTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void processData() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("354 Go\r\n");
        stringBuilder.append("250 OK\r\n");
        ConnectionMock connection = new ConnectionMock(stringBuilder);

        MessageEnvelope envelope = new MessageEnvelope();
        connection.getSession().addEnvelope(envelope);

        EnvelopeTransactionList transactionList = new EnvelopeTransactionList();
        transactionList.addTransaction("MAIL", "MAIL FROM:<tony@example.com>", "250 OK", false);
        connection.getSessionTransactionList().addEnvelope(transactionList);

        ClientData data = new ClientData();
        boolean process = data.process(connection);

        assertTrue(process);

        connection.parseLines();
        assertEquals("DATA\r\n", connection.getLine(1));
        assertEquals(".\r\n", connection.getLine(2));
    }

    @Test
    void processDataWithSubjectAndMessage() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("354 Go\r\n");
        stringBuilder.append("250 OK\r\n");
        ConnectionMock connection = new ConnectionMock(stringBuilder);

        MessageEnvelope envelope = new MessageEnvelope();
        envelope.setMail("tony@example.com");
        envelope.setRcpt("pepper@example.com");
        envelope.setSubject("Lost in space");
        envelope.setMessage("Rescue me!");
        connection.getSession().addEnvelope(envelope);

        EnvelopeTransactionList transactionList = new EnvelopeTransactionList();
        transactionList.addTransaction("MAIL", "MAIL FROM:<tony@example.com>", "250 OK", false);
        connection.getSessionTransactionList().addEnvelope(transactionList);

        ClientData data = new ClientData();
        boolean process = data.process(connection);

        assertTrue(process);

        connection.parseLines();
        assertEquals("DATA\r\n", connection.getLine(1));
        assertEquals("MIME-Version: 1.0\r\n", connection.getLine(2));
        assertEquals("From: <tony@example.com>\r\n", connection.getLine(5));
        assertEquals("To: <pepper@example.com>\r\n", connection.getLine(6));
        assertEquals("Subject: Lost in space\r\n", connection.getLine(7));
        assertEquals("\r\n", connection.getLine(10));
        assertEquals("Rescue me!\r\n", connection.getLine(11));
        assertEquals(".\r\n", connection.getLine(12));
    }

    @Test
    void processDataWithFile() throws IOException {
        processDataWithOptionalFileOrStream("src/test/resources/lipsum.eml", null);
    }

    @Test
    void processDataWithStream() throws IOException {
        processDataWithOptionalFileOrStream(null, new FileInputStream(new File("src/test/resources/lipsum.eml")));
    }

    void processDataWithOptionalFileOrStream(String file, InputStream stream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("354 Go\r\n");
        stringBuilder.append("250 OK\r\n");
        ConnectionMock connection = new ConnectionMock(stringBuilder);

        MessageEnvelope envelope = new MessageEnvelope();
        envelope.setFile(file);
        envelope.setStream(stream);
        connection.getSession().addEnvelope(envelope);

        EnvelopeTransactionList transactionList = new EnvelopeTransactionList();
        transactionList.addTransaction("MAIL", "MAIL FROM:<tony@example.com>", "250 OK", false);
        connection.getSessionTransactionList().addEnvelope(transactionList);

        ClientData data = new ClientData();
        boolean process = data.process(connection);

        assertTrue(process);

        connection.parseLines();
        assertEquals("DATA\r\n", connection.getLine(1));
        assertEquals("MIME-Version: 1.0\r\n", connection.getLine(2));
        assertEquals("Subject: Lipsum\r\n", connection.getLine(7));
        assertEquals("Content-Type: multipart/alternative;\r\n", connection.getLine(8));
        assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit.\r\n", connection.getLine(15));
        assertEquals("Donec vitae dapibus lacus.\r\n", connection.getLine(26));
        assertEquals("--MCBoundary11505141140170031\r\n", connection.getLine(30));
        assertEquals("<title>Lorem Ipsum</title>\r\n", connection.getLine(40));
        assertEquals("<br>Integer at finibus orci.\r\n", connection.getLine(51));
        assertEquals("--MCBoundary11505141140170031--\r\n", connection.getLine(66));    }

    @Test
    void processBdat() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("250 OK\r\n");
        ConnectionMock connection = new ConnectionMock(stringBuilder);
        connection.getSession().setEhloBdat(true);

        MessageEnvelope envelope = new MessageEnvelope();
        envelope.setChunkSize(100);
        connection.getSession().addEnvelope(envelope);

        EnvelopeTransactionList transactionList = new EnvelopeTransactionList();
        transactionList.addTransaction("MAIL", "MAIL FROM:<tony@example.com>", "250 OK", false);
        connection.getSessionTransactionList().addEnvelope(transactionList);

        ClientData data = new ClientData();
        boolean process = data.process(connection);

        assertFalse(process);
    }

    @Test
    void processBdatWithSubjectAndMessage() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("250 OK\r\n");
        ConnectionMock connection = new ConnectionMock(stringBuilder);
        connection.getSession().setEhloBdat(true);

        MessageEnvelope envelope = new MessageEnvelope();
        envelope.setMail("tony@example.com");
        envelope.setRcpt("pepper@example.com");
        envelope.setSubject("Lost in space");
        envelope.setMessage("Rescue me!");
        envelope.setChunkSize(512);
        connection.getSession().addEnvelope(envelope);

        EnvelopeTransactionList transactionList = new EnvelopeTransactionList();
        transactionList.addTransaction("MAIL", "MAIL FROM:<tony@example.com>", "250 OK", false);
        connection.getSessionTransactionList().addEnvelope(transactionList);

        ClientData data = new ClientData();
        boolean process = data.process(connection);

        assertTrue(process);

        connection.parseLines();
        assertEquals("BDAT 289 LAST\r\n", connection.getLine(1));
        assertEquals("MIME-Version: 1.0\r\n", connection.getLine(2));
        assertEquals("From: <tony@example.com>\r\n", connection.getLine(5));
        assertEquals("To: <pepper@example.com>\r\n", connection.getLine(6));
        assertEquals("Subject: Lost in space\r\n", connection.getLine(7));
        assertEquals("\r\n", connection.getLine(10));
        assertEquals("Rescue me!\r\n", connection.getLine(11));
    }

    @Test
    void processBdatWithFile() throws IOException {
        processBdatWithFileAndOptionalChunk(false);
    }

    @Test
    void processBdatWithFileAndChunkBdat() throws IOException {
        processBdatWithFileAndOptionalChunk(true);
    }

    void processBdatWithFileAndOptionalChunk(boolean chunkBdat) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("250 OK\r\n");
        stringBuilder.append("250 OK\r\n");
        stringBuilder.append("250 OK\r\n");
        stringBuilder.append("250 OK\r\n");
        stringBuilder.append("250 OK\r\n");
        ConnectionMock connection = new ConnectionMock(stringBuilder);
        connection.getSession().setEhloBdat(true);

        MessageEnvelope envelope = new MessageEnvelope();
        envelope.setFile("src/test/resources/lipsum.eml");
        envelope.setChunkSize(512);
        envelope.setChunkBdat(chunkBdat);
        connection.getSession().addEnvelope(envelope);

        EnvelopeTransactionList transactionList = new EnvelopeTransactionList();
        transactionList.addTransaction("MAIL", "MAIL FROM:<tony@example.com>", "250 OK", false);
        connection.getSessionTransactionList().addEnvelope(transactionList);

        ClientData data = new ClientData();
        boolean process = data.process(connection);

        assertFalse(process);

        connection.parseLines();
        assertEquals("BDAT 512\r\n", connection.getLine(1));
        assertEquals("MIME-Version: 1.0\r\n", connection.getLine(2));
        assertEquals("Subject: Lipsum\r\n", connection.getLine(7));
        assertEquals("Content-Type: multipart/alternative;\r\n", connection.getLine(8));
        assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit.\r\n", connection.getLine(15));
        assertEquals("VestibuluBDAT 512\r\n", connection.getLine(18));
        assertEquals("--MCBoundary11505141140170031\r\n", connection.getLine(32));
        assertEquals("<br>Integer at finibus BDAT 512\r\n", connection.getLine(54));
        assertEquals("--MCBoundary11505141140170031--\r\n", connection.getLine(71));
    }
}

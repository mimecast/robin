package com.mimecast.robin.smtp.connection;

import com.mimecast.robin.main.Factories;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.io.LineInputStream;
import com.mimecast.robin.smtp.session.Session;
import com.mimecast.robin.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("RedundantThrows")
public class ConnectionMock extends Connection {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final Map<Integer, String> lines = new HashMap<>();

    public ConnectionMock(StringBuilder string) {
        this();
        inc = new LineInputStream(new ByteArrayInputStream(string.toString().getBytes()));
        out = new DataOutputStream(output);
    }

    public ConnectionMock() {
        this(Factories.getSession());
    }

    public ConnectionMock(Session session) {
        super(session);
    }

    @Override
    public void connect() throws IOException {
        String read = read("220");
        session.getSessionTransactionList().addTransaction("SMTP", read, !read.startsWith("220"));
    }

    @Override
    public void buildStreams() throws IOException {
        // Do nothing.
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getOutput() {
        return output.toString();
    }

    public void parseLines() throws IOException {
        lines.putAll(StreamUtils.parseLines(output));
    }

    public String getLine(int lineNo) {
        return lines.get(lineNo);
    }

    public static ConnectionMock getConnection(StringBuilder stringBuilder) {
        ConnectionMock connection = new ConnectionMock(stringBuilder);
        connection.getSession().setEhlo("example.com");
        connection.getSession().setMx(Collections.singletonList("example.com"));
        connection.getSession().setPort(25);

        MessageEnvelope envelope = new MessageEnvelope();
        envelope.setMail("tony@example.com");
        envelope.setRcpt("pepper@example.com");
        envelope.setSubject("Lost in space");
        envelope.setMessage("Rescue me!");
        connection.getSession().addEnvelope(envelope);

        return connection;
    }
}

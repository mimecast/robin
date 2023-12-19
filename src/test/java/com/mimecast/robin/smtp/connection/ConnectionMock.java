package com.mimecast.robin.smtp.connection;

import com.mimecast.robin.main.Factories;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.io.LineInputStream;
import com.mimecast.robin.smtp.session.Session;
import com.mimecast.robin.util.StreamUtils;

import javax.net.ssl.SSLSocket;
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

    public ConnectionMock setSocket(Socket socket) {
        this.socket = socket;
        return this;
    }

    public Socket getSocket() {
        return socket;
    }

    String peerHost;

    public ConnectionMock setPeerHost(String peerHost) {
        this.peerHost = peerHost;
        return this;
    }

    @Override
    public String getPeerHost() {
        return socket instanceof SSLSocket ? ((SSLSocket) socket).getSession().getPeerHost() : peerHost;
    }

    String protocol;

    public ConnectionMock setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    @Override
    public String getProtocol() {
        return socket instanceof SSLSocket ? ((SSLSocket) socket).getSession().getProtocol() : protocol;
    }

    String cipherSuite;

    public ConnectionMock setCipherSuite(String cipherSuite) {
        this.cipherSuite = cipherSuite;
        return this;
    }

    @Override
    public String getCipherSuite() {
        return socket instanceof SSLSocket ? ((SSLSocket) socket).getSession().getCipherSuite() : cipherSuite;
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

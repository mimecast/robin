package com.mimecast.robin.smtp.connection;

import com.mimecast.robin.main.Factories;
import com.mimecast.robin.smtp.io.LineInputStream;
import com.mimecast.robin.smtp.session.Session;
import com.mimecast.robin.smtp.transaction.SessionTransactionList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("RedundantThrows")
public class ConnectionMock extends Connection {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final Map<Integer, String> lines = new HashMap<>();

    public ConnectionMock(StringBuilder string) {
        super(Factories.getSession());
        inc = new LineInputStream(new ByteArrayInputStream(string.toString().getBytes()));
        out = new DataOutputStream(output);
    }

    public ConnectionMock(Session session, SessionTransactionList sessionTransactionList) {
        super(session, sessionTransactionList);
    }

    public ConnectionMock(Session session) {
        super(session);
    }

    @Override
    public void connect() throws IOException {
        String read = read("220");
        getSessionTransactionList().addTransaction("SMTP", read, !read.startsWith("220"));
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
        LineInputStream stream = new LineInputStream(new ByteArrayInputStream(output.toByteArray()));

        byte[] bytes;
        while ((bytes = stream.readLine()) != null) {
            lines.put(stream.getLineNumber(), new String(bytes));
        }
    }

    public String getLine(int lineNo) {
        return lines.get(lineNo);
    }
}

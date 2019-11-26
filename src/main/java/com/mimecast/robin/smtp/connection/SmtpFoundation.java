package com.mimecast.robin.smtp.connection;

import com.mimecast.robin.main.Factories;
import com.mimecast.robin.smtp.io.LineInputStream;
import com.mimecast.robin.util.Random;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLSocket;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SMTP foundation for socket reads and writes.
 * <p>Provides basic socket functionalities for building SMTP servers and clients.
 * @link https://tools.ietf.org/html/rfc5321 RFC5321
 *
 * @see Connection
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public abstract class SmtpFoundation {
    private static final Logger log = LogManager.getLogger(SmtpFoundation.class);

    /**
     * Socket default timeout.
     */
    public static final int DEFAULTTIMEOUT = 30000;

    /**
     * Socket extended timeout.
     * <p>Used for extendedRead().
     * <p>Handy for SMTP DATA and BDAT extensions.
     */
    public static final int EXTENDEDTIMEOUT = 120000;

    /**
     * Socket instance.
     */
    Socket socket;

    /**
     * Socket input stream container.
     */
    LineInputStream inc;

    /**
     * Socket output stream container.
     */
    DataOutputStream out;

    /**
     * Default TLS protocols supported as string array.
     */
    private String[] protocols;

    /**
     * Default TLS cipher suites supported as string array.
     */
    private String[] ciphers;

    /**
     * Constants.
     */
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final int DASH = 45;
    private static final int FULLSTOP = 46;

    /**
     * Logging constant.
     */
    private static final String LOG_WRITE = ">> {}";

    /**
     * @param timeout Time in milliseconds.
     * @throws IOException Unable to communicate.
     */
    public void setTimeout(int timeout) throws IOException {
        socket.setSoTimeout(timeout);
    }

    /**
     * Sets TLS protocols supported.
     *
     * @param protocols Protocols list.
     */
    public void setProtocols(String[] protocols) {
        this.protocols = protocols;
    }

    /**
     * Sets TLS ciphers supported.
     *
     * @param ciphers Cipher suites list.
     */
    public void setCiphers(String[] ciphers) {
        this.ciphers = ciphers;
    }

    /**
     * Gets TLS protocol used if TLS negociated.
     *
     * @return Protocol string.
     */
    public String getProtocol() {
        return socket instanceof SSLSocket ? ((SSLSocket) socket).getSession().getProtocol() : "";
    }

    /**
     * Gets TLS cipher suite used if TLS negociated.
     *
     * @return Cipher suite string.
     */
    public String getCipherSuite() {
        return socket instanceof SSLSocket ? ((SSLSocket) socket).getSession().getCipherSuite() : "";
    }

    /**
     * Read from socket without expecting a particular response code.
     *
     * @return String read from buffer.
     * @throws IOException Unable to communicate.
     */
    public String read() throws IOException {
        return read("", false);
    }

    /**
     * Read from socket expecting a particular response code.
     *
     * @param code Expected SMTP response code.
     * @return String read from buffer.
     * @throws IOException Unable to communicate.
     */
    public String read(String code) throws IOException {
        return read(code, false);
    }

    /**
     * Read multiple lines from socket.
     *
     * @param multiline True to enable.
     * @return String read from buffer.
     * @throws IOException Unable to communicate.
     */
    public String read(boolean multiline) throws IOException {
        return read("", multiline);
    }

    /**
     * Read fixed number of bytes from socket.
     *
     * @param bytesToRead  Number of bytes to read.
     * @param outputStream Output stream.
     * @throws IOException Unable to communicate.
     */
    public void read(int bytesToRead, ByteArrayOutputStream outputStream) throws IOException {
        for (int i = 0; i < bytesToRead; i++) {
            outputStream.write((byte) inc.read());
        }
    }

    /**
     * Read from socket.
     *
     * @param expectedCode Expected SMTP response code.
     * @return String read from buffer.
     * @throws IOException Unable to communicate.
     */
    private String read(String expectedCode, boolean multiline) throws IOException {
        StringBuilder received = new StringBuilder();
        String receivedCode = "";

        try {
            byte[] read;
            boolean stop;
            while ((read = inc.readLine()) != null) {
                log.info("<< {}", StringUtils.stripEnd(new String(read, UTF_8), null));

                if (multiline) {
                    String str =new String(read);
                    received.append(str);
                    stop = isFullStop(str);
                }
                else {
                    receivedCode = new String(read).substring(0, expectedCode.length());
                    received.append(new String(read));
                    stop = isSmtpStop(read);
                }

                if (stop) {
                    break;
                }
            }
        } catch (IOException e) {
            log.error("Failed to read from socket.");
            throw e;
        }

        if (expectedCode.length() == 3 && !expectedCode.equalsIgnoreCase(receivedCode)) {
            if (receivedCode.trim().length() == 0) {
                log.warn("Got no response but expected {}", expectedCode);
            } else {
                log.warn("Got response code {} but expected {}", receivedCode, expectedCode);
            }
        }

        return received.toString();
    }

    /**
     * Check for fullstop.
     *
     * @param string String.
     * @return True if found.
     */
    private boolean isFullStop(String string) {
        byte[] check = string.trim().getBytes();
        return check.length == 1 && check[0] == FULLSTOP;
    }

    /**
     * Check for SMTP multiline last line.
     *
     * @param bytes Byte array.
     * @return True if last line.
     */
    private boolean isSmtpStop(byte[] bytes) {
        return bytes.length < 4 || bytes[3] != DASH;
    }

    /**
     * Write to a socket via the instance DataOutputStream.
     *
     * @param string String to write to socket.
     * @throws IOException Unable to communicate.
     */
    public void write(String string) throws IOException {
        try {
            out.write((string + "\r\n").getBytes(UTF_8));
            log.info(LOG_WRITE, string);
        } catch (IOException e) {
            log.error("Failed to write to socket.");
            throw e;
        }
    }

    /**
     * Write to a socket via the instance DataOutputStream.
     * <p>Used for BDAT deliveries.
     *
     * @param bytes String to write to socket.
     * @throws IOException Unable to communicate.
     */
    public void write(byte[] bytes) throws IOException {
        write(bytes, false);
    }

    /**
     * Write to a socket via the instance DataOutputStream.
     * <p>Used for BDAT deliveries.
     *
     * @param bytes      String to write to socket.
     * @param chunkWrite True to chunk string into multiple uneven writes.
     * @throws IOException Unable to communicate.
     */
    public void write(byte[] bytes, boolean chunkWrite) throws IOException {
        try {
            int totalBytes = bytes.length;

            // Chunk payload into multiple uneven length writes.
            if (chunkWrite && totalBytes >= 2048) {
                // Calculate chunks between min and max.
                int min = 1024;
                int max = 2048;

                List<Integer> chunks = new ArrayList<>();
                int random;
                do {
                    random = min + Random.no(max - min);
                    totalBytes -= random;
                    chunks.add(random);
                } while(totalBytes > max);
                chunks.add(totalBytes);

                // Write chunks to socket and log.
                int from = 0;
                int to;
                byte[] write;
                for (Integer chunk : chunks) {
                    to = from + chunk;
                    write = Arrays.copyOfRange(bytes, from, to);
                    from = to;
                    out.write(write);
                    log.trace(LOG_WRITE, StringUtils.stripEnd(new String(write, UTF_8), null));
                }
            } else {
                out.write(bytes);
                log.trace(LOG_WRITE, StringUtils.stripEnd(new String(bytes, UTF_8), null));
            }
        } catch (IOException e) {
            log.error("Failed to write to socket.");
            throw e;
        }
    }

    /**
     * Write from given InputStream.
     * <p>Used for DATA deliveries.
     * <p>Implements dot stuffing.
     * <p>See RFC5321 Section 4.5.2.
     *
     * @param inputStream Input stream.
     * @throws IOException Unable to communicate.
     */
    public void stream(LineInputStream inputStream) throws IOException {
        String string;
        byte[] bytes;
        while ((bytes = inputStream.readLine()) != null) {
            string = new String(bytes).trim();

            // Dot stuffing.
            if (string.equals(".")) {
                out.write(".".getBytes(UTF_8));
            }

            out.write(bytes);
            log.trace(LOG_WRITE, StringUtils.stripEnd(new String(bytes, UTF_8).replaceAll("\\s+$",""), null));
        }
        out.write("\r\n".getBytes(UTF_8));
    }

    /**
     * Enable encryption for the given socket.
     *
     * @param client True if in client mode.
     * @throws SmtpException SMTP delivery exception.
     */
    public void startTLS(boolean client) throws SmtpException {
        try {
            socket = Factories.getTLSSocket()
                    .setSocket(socket)
                    .setProtocols(protocols)
                    .setCiphers(ciphers)
                    .startTLS(client);
        } catch (Exception e) {
            log.error("Failed to start {} TLS: {}", (client ? "client" : "server"), e.getMessage());
            close();
            throw new SmtpException(e);
        }
    }

    /**
     * Close socket.
     */
    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                log.info("Socket closed.");
            }
        } catch (IOException e) {
            log.warn("Socket already closed.");
        }
    }
}

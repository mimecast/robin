package com.mimecast.robin.storage;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.main.Factories;
import com.mimecast.robin.mime.EmailParser;
import com.mimecast.robin.mime.headers.MimeHeader;
import com.mimecast.robin.smtp.EmailDelivery;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.smtp.session.Session;
import com.mimecast.robin.util.PathUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.internet.InternetAddress;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Local storage client implementation.
 *
 * <p>Saves files on disk.
 */
public class LocalStorageClient implements StorageClient {
    protected static final Logger log = LogManager.getLogger(LocalStorageClient.class);

    /**
     * Enablement.
     */
    protected final boolean enabled = Config.getServer().getStorage().getBooleanProperty("enabled");

    /**
     * UID.
     */
    protected final String uid = UUID.randomUUID().toString();

    /**
     * Connection instance.
     */
    protected Connection connection;

    /**
     * Save file name.
     */
    protected String fileName;

    /**
     * Save file path.
     */
    protected String path;

    /**
     * EmailParser instance.
     */
    protected EmailParser parser;

    /**
     * Save file output stream.
     */
    protected OutputStream stream = NullOutputStream.NULL_OUTPUT_STREAM;

    /**
     * Sets file extension.
     *
     * @param extension File extension.
     * @return Self.
     */
    public LocalStorageClient setExtension(String extension) {
        String now = new SimpleDateFormat("yyyyMMdd", Locale.UK).format(new Date());

        if (extension == null) {
            extension = ".dat";
        } else if (!extension.startsWith(".")) {
            extension = "." + extension;
        }

        fileName = now + "." + uid + extension;
        path = Config.getServer().getStorage().getStringProperty("path", "/tmp/store");

        return this;
    }

    /**
     * Sets connection.
     *
     * @param connection Connection instance.
     * @return Self.
     */
    @Override
    public LocalStorageClient setConnection(Connection connection) {
        this.connection = connection;

        // Append first recipient domain/address to path
        if (connection != null && !connection.getSession().getRcpts().isEmpty()) {
            String[] splits = connection.getSession().getRcpts().get(0).getAddress().split("@");
            if (splits.length == 2) {
                path = Paths.get(
                        path,
                        PathUtils.normalize(splits[1]),
                        PathUtils.normalize(splits[0])
                ).toString();
            }
        }

        return this;
    }

    /**
     * Gets file output stream.
     *
     * @return OutputStream instance.
     */
    @Override
    public OutputStream getStream() throws FileNotFoundException {
        if (enabled) {
            if (PathUtils.makePath(path)) {
                stream = new FileOutputStream(Paths.get(path, fileName).toString());
            } else {
                log.error("Storage path could not be created");
            }
        } else {
            stream = NullOutputStream.NULL_OUTPUT_STREAM;
        }

        return stream;
    }

    /**
     * Gets file token.
     *
     * @return String.
     */
    @Override
    public String getToken() {
        return Paths.get(path, fileName).toString();
    }

    /**
     * Gets UID.
     *
     * @return String.
     */
    @Override
    public String getUID() {
        return uid;
    }

    /**
     * Saves file.
     */
    @Override
    public void save() {
        // TODO Store token in connection session envelope.
        if (enabled) {
            try {
                parser = new EmailParser(getToken()).parse(true);
                rename();
                relay();

            } catch (IOException e) {
                log.error("Storage unable to parse email: {}", e.getMessage());
            }

            try {
                stream.flush();
                stream.close();

            } catch (IOException e) {
                log.error("Storage file not flushed/closed: {}", e.getMessage());
            }
        }
    }

    /**
     * Rename filename.
     * <p>Will parse and lookup if a X-Robin-Filename header exists and use it's value as a filename.
     *
     * @throws IOException Unable to delete file.
     */
    private void rename() throws IOException {
        Optional<MimeHeader> optional = parser.getHeaders().get("x-robin-filename");
        if (optional.isPresent()) {
            MimeHeader header = optional.get();

            String source = getToken();
            Path target = Paths.get(path, header.getValue());

            if (StringUtils.isNotBlank(header.getValue())) {
                if (Files.deleteIfExists(target)) {
                    log.info("Storage deleted existing file before rename");
                }

                if (new File(source).renameTo(new File(target.toString()))) {
                    fileName = header.getValue();
                    log.info("Storage moved file to: {}", getToken());
                }
            }
        }
    }

    /**
     * Relay email.
     * <p>Will relay email to provided server.
     */
    private void relay() {
        Optional<MimeHeader> optional = parser.getHeaders().get("x-robin-relay");
        if (optional.isPresent()) {
            MimeHeader header = optional.get();
            String mx;
            int port = 25;
            if (header.getValue().contains(":")) {
                String[] splits = header.getValue().split(":");
                mx = splits[0];
                if (splits.length > 1) {
                    port = Integer.parseInt(splits[1]);
                }
            } else {
                mx = header.getValue();
            }
            log.info("Relay found for: {}:{}", mx, port);

            MessageEnvelope envelope = new MessageEnvelope()
                    .setMail(connection.getSession().getMail().getAddress())
                    .setRcpts(connection.getSession().getRcpts()
                            .stream()
                            .map(InternetAddress::getAddress)
                            .collect(Collectors.toList()))
                    .setFile(getToken());

            Session session = Factories.getSession() // TODO Add session cloning.
                    .setMx(Collections.singletonList(mx))
                    .setPort(port)
                    .addEnvelope(envelope);

            new Thread(() -> new EmailDelivery(session).send()).start();
        }
    }
}

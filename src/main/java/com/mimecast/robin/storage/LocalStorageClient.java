package com.mimecast.robin.storage;

import com.mimecast.robin.main.Config;
import com.mimecast.robin.mime.EmailParser;
import com.mimecast.robin.mime.headers.MimeHeader;
import com.mimecast.robin.smtp.connection.Connection;
import com.mimecast.robin.util.PathUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Local storage client implementation.
 *
 * <p>Saves files on disk.
 */
public class LocalStorageClient implements StorageClient {
    protected static final Logger log = LogManager.getLogger(LocalStorageClient.class);

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
     * Save file output stream.
     */
    protected OutputStream stream = new NullOutputStream();

    /**
     * Local storage client.
     *
     * @param extension File extension.
     */
    public LocalStorageClient(String extension) {
        String now = new SimpleDateFormat("yyyyMMdd", Locale.UK).format(new Date());

        if (extension == null) {
            extension = ".dat";
        } else if (!extension.startsWith(".")) {
            extension = "." + extension;
        }

        fileName = now + "." + uid + extension;
        path = Config.getServer().getStorageDir();
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
        if (PathUtils.makePath(path)) {
            stream = new FileOutputStream(Paths.get(path, fileName).toString());
        } else {
            log.error("Storage path could not be created");
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
        try {
            stream.flush();
            stream.close();
            rename();

        } catch (IOException e) {
            log.error("Storage file not flushed/closed: {}", e.getMessage());
        }
    }

    /**
     * Rename filename.
     * <p>Will parse and lookup if a X-Robin-Filename header exists and use it's value as a filename.
     */
    private void rename() {
        try {
            EmailParser parser = new EmailParser(getToken())
                    .parse(true);

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

        } catch (IOException e) {
            log.error("Storage unable to parse email: {}", e.getMessage());
        }
    }
}

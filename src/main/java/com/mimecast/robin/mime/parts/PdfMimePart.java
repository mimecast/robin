package com.mimecast.robin.mime.parts;

import com.mimecast.robin.config.assertion.MimeConfig;
import com.mimecast.robin.mime.headers.MimeHeader;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.util.PathUtils;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * MIME part container from pdf config.
 */
public class PdfMimePart extends MimePart {

    /**
     * MimeConfig instance.
     */
    protected final MimeConfig config;

    /**
     * messageEnvelope instance.
     */
    protected final MessageEnvelope envelope;

    /**
     * Constructs a new FileMimePart instance with given BasicConfig instance.
     *
     * @param config   MimeConfig instance.
     * @param envelope MessageEnvelope instance.
     * @throws IOException Unable to open/read from file.
     */
    public PdfMimePart(MimeConfig config, MessageEnvelope envelope) throws IOException {
        this.config = config;
        config.getHeaders().forEach(h -> headers.put(h)); // Add headers.

        this.envelope = envelope;
        build();
    }

    /**
     * Builds PDF.
     *
     * @throws IOException Unable to open/read from file.
     */
    protected void build() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            XRLog.listRegisteredLoggers().forEach(logger -> {
                if (logger.contains("com.openhtmltopdf")) XRLog.setLevel(logger, Level.WARNING);
            });

            new PdfRendererBuilder()
                    .withHtmlContent(html(), System.getProperty("user.dir"))
                    .toStream(baos)
                    .run();

            body = new ByteArrayInputStream(baos.toByteArray());
        }
    }

    /**
     * Builds HTML.
     *
     * @return String.
     * @throws IOException Unable to open/read from file.
     */
    protected String html() throws IOException {
        StringBuilder html = new StringBuilder()
                .append("<html>")
                .append("<head>")
                .append("<style>")
                .append("body {padding: 40px;}")
                .append("</style>")
                .append("</head>")
                .append("<body>");

        if (config.hasProperty("text")) {
            html.append("<p>");
            html.append(config.getStringProperty("text"));
            html.append("</p>");
        }

        if (config.hasProperty("folder")) {
            html.append("<p><img src=\"file://");
            html.append(Paths.get(System.getProperty("user.dir"), PathUtils.folderFile(config.getStringProperty("folder"), Arrays.asList("jpg", "jpeg", "png", "gif"))));
            html.append("\"/></p>");

        } else if (config.hasProperty("image")) {
            html.append("<p><img src=\"file://");
            html.append(Paths.get(System.getProperty("user.dir"), config.getStringProperty("image")));
            html.append("\"/></p>");
        }

        html.append("</body>")
                .append("</html>");

        return html.toString();
    }

    /**
     * Writes email to given output stream.
     *
     * @param outputStream OutputStream instance.
     * @return Self.
     * @throws IOException Unable to open/read from file or write to output stream.
     */
    @Override
    public MimePart writeTo(OutputStream outputStream) throws IOException {
        // Ensure we have a Content-Type header.
        MimeHeader contentType = getHeader("Content-Type");
        if (contentType == null) {
            headers.put(new MimeHeader("Content-Type", "application/pdf"));
        }

        super.writeTo(outputStream);

        return this;
    }
}

package com.mimecast.robin.mime.parts;

import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.mime.headers.MimeHeader;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.io.MagicInputStream;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.logging.Level;

/**
 * MIME part container from pdf config.
 */
public class PdfMimePart extends MimePart {

    /**
     * Config instance.
     */
    protected final BasicConfig config;

    /**
     * messageEnvelope instance.
     */
    protected final MessageEnvelope envelope;

    /**
     * Constructs a new FileMimePart instance with given BasicConfig instance.
     *
     * @param config   BasicConfig instance.
     * @param envelope MessageEnvelope instance.
     * @throws IOException Unable to open/read from file.
     */
    public PdfMimePart(BasicConfig config, MessageEnvelope envelope) throws IOException {
        this.config = config;
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
        if (config.hasProperty("image")) {
            html.append("<p><img src=\"file://");
            html.append(Paths.get(System.getProperty("user.dir"), config.getStringProperty("image")));
            html.append("\"/></p>");
        }

        html.append("</body>")
                .append("</html>");

        MagicInputStream magicInputStream = new MagicInputStream(new ByteArrayInputStream(html.toString().getBytes()), envelope);

        byte[] bytes;
        StringBuilder magicHtml = new StringBuilder();
        while ((bytes = magicInputStream.readLine()) != null) {
            magicHtml.append(new String(bytes));
        }

        return magicHtml.toString();
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

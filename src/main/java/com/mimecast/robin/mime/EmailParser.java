package com.mimecast.robin.mime;

import com.mimecast.robin.mime.headers.MimeHeader;
import com.mimecast.robin.mime.headers.MimeHeaders;
import com.mimecast.robin.mime.parts.FileMimePart;
import com.mimecast.robin.mime.parts.MimePart;
import com.mimecast.robin.mime.parts.MultipartMimePart;
import com.mimecast.robin.mime.parts.TextMimePart;
import com.mimecast.robin.smtp.io.LineInputStream;
import com.mimecast.robin.util.QuotedPrintableDecoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Basic email MIME parser.
 * Documented here:
 * https://mimecast.jira.com/wiki/spaces/MESSAGINGSERVICES/pages/1990066238/Robin+Mime+Parser
 */
public class EmailParser {
    private static final Logger log = LogManager.getLogger(EmailParser.class);

    /**
     * Email stream.
     */
    private final LineInputStream stream;

    /**
     * Parsed headers.
     */
    private final MimeHeaders headers = new MimeHeaders();

    /**
     * Parsed parts.
     */
    private final List<MimePart> parts = new ArrayList<>();

    /**
     * Constructs new EmailParser instance with given path.
     *
     * @param path Path to email.
     * @throws FileNotFoundException File not found.
     */
    public EmailParser(String path) throws FileNotFoundException {
        this.stream = new LineInputStream(new FileInputStream(path));
    }

    /**
     * Constructs new EmailParser instance with given path.
     *
     * @param stream Email LineInputStream instance.
     */
    public EmailParser(LineInputStream stream) {
        this.stream = stream;
    }

    /**
     * Gets headers.
     *
     * @return MimeHeaders instance.
     */
    public MimeHeaders getHeaders() {
        return headers;
    }

    /**
     * Gets parts.
     *
     * @return List of MimePart.
     */
    public List<MimePart> getParts() {
        return parts;
    }

    /**
     * Parses email.
     *
     * @return Self.
     * @throws IOException File read/write issue.
     */
    public EmailParser parse() throws IOException {
        return parse(false);
    }

    /**
     * Parses email.
     *
     * @param headersOnly Parse only headers.
     * @return Self.
     * @throws IOException File read/write issue.
     */
    public EmailParser parse(boolean headersOnly) throws IOException {
        parseHeaders();

        if (!headersOnly) {
            parseBody();
        }

        return this;
    }

    /**
     * Parses email headers.
     *
     * @throws IOException File read/write issue.
     */
    private void parseHeaders() throws IOException {
        byte[] bytes;
        StringBuilder header = new StringBuilder();
        while ((bytes = stream.readLine()) != null) {

            String line = new String(bytes);

            // If line doens't start with a whitespace
            // we need to produce a header from what we got so far
            // if any.
            if (!Character.isWhitespace(bytes[0]) && header.length() > 0) {
                headers.put(new MimeHeader(header.toString()));
                header = new StringBuilder();
            }

            // Break if found end of headers.
            if (StringUtils.isBlank(line.trim())) {
                break;
            }

            header.append(line);
        }

        // Last header
        if (header.length() > 0) {
            headers.put(new MimeHeader(header.toString()));
        }
    }

    /**
     * Parses email body.
     */
    private void parseBody() throws IOException {
        Optional<MimeHeader> optional = headers.get("Content-Type");
        if (optional.isPresent()) {
            MimeHeader hdr = optional.get();
            String boundary = hdr.getParameter("boundary");

            MimePart part;
            if (hdr.getValue().startsWith("multipart/")) {
                part = new MultipartMimePart();

            } else if (hdr.getValue().startsWith("text/")) {
                part = parsePartContent(true, headers, boundary);

            } else {
                part = parsePartContent(false, headers, boundary);
            }

            part.addHeader(hdr.getName(), hdr.getValue());
            parts.add(part);

            if (boundary == null || boundary.length() == 0) {
                parsePartContent(true, headers, "");
            } else {
                parsePart(boundary);
            }
        }
    }

    /**
     * Gets filename.
     *
     * @param headers MimeHeaders instance.
     * @return String.
     */
    private String getFileName(MimeHeaders headers) {
        Optional<MimeHeader> optional = headers.get("Content-Disposition");
        if (optional.isPresent() && optional.get().getParameter("filename") != null) {
            return optional.get().getParameter("filename");
        }

        optional = headers.get("Content-Type");
        if (optional.isPresent()) {
            MimeHeader header = optional.get();

            String name = header.getParameter("name");
            if (name != null) {
                return optional.get().getParameter("name");
            }

            String type = header.getCleanValue();
            if (type.equalsIgnoreCase("text/html")) {
                return "part." + parts.size() + ".html";

            } else if (type.equalsIgnoreCase("text/plain")) {
                return "part." + parts.size() + ".txt";

            } else if (type.equalsIgnoreCase("text/calendar")) {
                return "part." + parts.size() + ".cal";

            } else if (type.toLowerCase().startsWith("image/")) {
                return "part." + parts.size() + ".img";

            } else if (type.toLowerCase().startsWith("message/")) {
                return "rfc822." + parts.size() + ".eml";

            } else {
                return "part." + parts.size() + ".dat";
            }

        }

        return "";
    }

    /**
     * Parse part by boundary.
     *
     * @param boundary Boundary string.
     * @throws IOException File read/write issue.
     */
    private void parsePart(String boundary) throws IOException {
        MimeHeaders partHeaders = new MimeHeaders();
        byte[] bytes;
        StringBuilder header = new StringBuilder();
        while ((bytes = stream.readLine()) != null) {

            String line = new String(bytes);

            // If line doens't start with a whitespace
            // we need to produce a header from what we got so far
            // if any.
            if (!Character.isWhitespace(bytes[0]) && header.length() > 0) {
                if (header.toString().trim().length() > 0) {
                    partHeaders.put(new MimeHeader(header.toString().trim()));
                }
                header = new StringBuilder();
            }

            // Break if found end of headers.
            if (StringUtils.isBlank(line.trim())) {

                // If line doens't start with a whitespace
                // we need to produce a header from what we got so far
                // if any.
                if (header.toString().trim().length() > 0) {
                    partHeaders.put(new MimeHeader(header.toString().trim()));
                }

                header = new StringBuilder();

                // Find out which part it is.
                MimePart part;
                Optional<MimeHeader> optional = partHeaders.get("Content-Type");
                if (optional.isPresent()) {
                    MimeHeader ct = optional.get();

                    if (ct.getValue().startsWith("multipart/")) {
                        String currentBoundary = ct.getParameter("boundary");
                        parsePart(currentBoundary);

                    } else {
                        part = parsePartContent(ct.getValue().startsWith("text/"), partHeaders, boundary);

                        for (MimeHeader hdr : partHeaders.get()) {
                            part.addHeader(hdr.getName(), hdr.getValue().trim());
                        }
                        parts.add(part);

                        partHeaders = new MimeHeaders();
                    }
                }
            }

            header.append(line);
        }

        // Last header.
        if (header.length() > 0) {
            headers.put(new MimeHeader(header.toString().trim()));
        }
    }

    /**
     * Parse part content.
     *
     * @param isTextPart Is text/* type?
     * @param headers    MimeHeaders instance.
     * @param boundary   Boundary string.
     * @return MimePart instance.
     * @throws IOException File read/write issue.
     */
    private MimePart parsePartContent(boolean isTextPart, MimeHeaders headers, String boundary) throws IOException {
        boolean isBase64 = false;
        boolean isQuotedPrintable = false;

        // Get encoding.
        Optional<MimeHeader> cte = headers.get("content-transfer-encoding");
        if (cte.isPresent()) {
            String encoding = cte.get().getValue();

            isBase64 = encoding.compareToIgnoreCase("base64") == 0;
            isQuotedPrintable = encoding.compareToIgnoreCase("quoted-printable") == 0;
        }

        try {
            // Buid digests.
            MessageDigest digestSha1 = MessageDigest.getInstance(HashType.SHA_1.getKey());
            MessageDigest digestSha256 = MessageDigest.getInstance(HashType.SHA_256.getKey());
            MessageDigest digestMD5 = MessageDigest.getInstance(HashType.MD_5.getKey());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ByteArrayOutputStream content = new ByteArrayOutputStream();

            byte[] bytes;
            while ((bytes = stream.readLine()) != null) {
                String line = new String(bytes);
                if (boundary != null && boundary.length() > 0 && line.contains(boundary)) {
                    break;
                }

                baos.write(bytes);
            }

            // Decode if needed.
            if (isBase64) {
                content.write(Base64.decodeBase64(baos.toByteArray()));

            } else if (isQuotedPrintable) {
                try {
                    content.write(QuotedPrintableDecoder.decode(baos.toByteArray()));

                } catch (DecoderException de) {
                    log.error("EmailParser decoder exception:", de);
                    content.write(baos.toByteArray());
                }
            } else {
                content.write(baos.toByteArray());
            }
            baos.close();

            // Construct part.
            MimePart part;
            if (isTextPart) {
                part = new TextMimePart(content.toByteArray());

            } else {
                part = new FileMimePart();
            }

            // Set part details.
            digestSha1.update(content.toByteArray());
            part.setHash(HashType.SHA_1, Base64.encodeBase64String(digestSha1.digest()));

            digestSha256.update(content.toByteArray());
            part.setHash(HashType.SHA_256, Base64.encodeBase64String(digestSha256.digest()));

            digestMD5.update(content.toByteArray());
            part.setHash(HashType.MD_5, Base64.encodeBase64String(digestMD5.digest()));

            part.setSize(content.size());

            return part;

        } catch (NoSuchAlgorithmException nsae) {
            throw new IOException("No such algorithm", nsae);
        }
    }
}

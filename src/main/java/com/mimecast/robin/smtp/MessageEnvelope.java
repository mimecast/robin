package com.mimecast.robin.smtp;

import com.mimecast.robin.config.assertion.AssertConfig;
import com.mimecast.robin.config.assertion.MimeConfig;
import com.mimecast.robin.main.Config;
import com.mimecast.robin.util.PathUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Message envelope.
 *
 * <p>This is the container for SMTP envelopes.
 * <p>It will store the meta data associated with each email sent.
 */
public class MessageEnvelope {

    // Set MAIL FROM and RCPT TO.
    private String mail = null;
    private String rcpt = null;
    private List<String> rcpts = new ArrayList<>();
    private final Map<String, List<String>> params = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    private boolean prependHeaders = false;

    // Set MimeConfig.
    private MimeConfig mime = null;

    // Set EML file, folder or null.
    private String file = null;
    private String folder = null;

    // Set EML stream or null.
    private InputStream stream = null;

    // If EML is null set subject and message.
    private String subject = null;
    private String message = null;

    private final String date;
    private final String msgId;

    private int chunkSize = 0;
    private boolean chunkBdat = false;
    private boolean chunkWrite = false;

    private int terminateAfterBytes = 0;
    private boolean terminateBeforeDot = false;
    private boolean terminateAfterDot = false;

    private int slowBytes = 1;
    private int slowWait = 0;

    private int repeat = 0;

    // Assertions to be made against transaction.
    private AssertConfig assertConfig;

    /**
     * Constructs a new MessageEnvelope instance.
     */
    public MessageEnvelope() {
        date = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z", Config.getProperties().getLocale()).format(new Date());
        String now = String.valueOf(System.currentTimeMillis());
        String uid = UUID.randomUUID() + "-" + now;

        int size = 50 + 31 - date.length(); // Fixed length for unit tests stability.
        msgId = StringUtils.leftPad(uid, size, "0");
    }

    /**
     * Gets date.
     *
     * @return Date string.
     */
    public String getDate() {
        return date;
    }

    /**
     * Gets yyyyMMdd date.
     *
     * @return Date string.
     */
    public String getYymd() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date());
    }

    /**
     * Gets current year.
     *
     * @return Year string.
     */
    public String getYear() {
        return new SimpleDateFormat("yyyy").format(new Date());
    }

    /**
     * Gets Message-ID.
     *
     * @return Message-ID string.
     */
    public String getMessageId() {
        return msgId;
    }

    /**
     * Gets MAIL FROM.
     *
     * @return MAIL FROM address.
     */
    public String getMailFrom() {
        if (StringUtils.isNotBlank(mail)) {
            return mail;
        }
        return "";
    }

    /**
     * Gets RCPT TO.
     *
     * @return RCPT TO address.
     */
    public String getRcptTo() {
        if (StringUtils.isNotBlank(rcpt)) {
            return rcpt;
        } else if (!rcpts.isEmpty()) {
            return rcpts.get(0);
        }
        return "";
    }

    /**
     * Gets MAIL.
     *
     * @return MAIL address.
     */
    public String getMail() {
        return mail;
    }

    /**
     * Sets MAIL.
     *
     * @param mail MAIL address.
     * @return Self.
     */
    public MessageEnvelope setMail(String mail) {
        this.mail = mail;
        return this;
    }

    /**
     * Gets RCPT.
     *
     * @return RCPT address.
     */
    public String getRcpt() {
        return rcpt;
    }

    /**
     * Sets RCPT.
     *
     * @param rcpt RCPT address.
     * @return Self.
     */
    public MessageEnvelope setRcpt(String rcpt) {
        this.rcpt = rcpt;
        return this;
    }

    /**
     * Gets recipients addresses.
     *
     * @return Recipients address list.
     */
    public List<String> getRcpts() {
        if (rcpt != null && !rcpts.contains(rcpt)) {
            rcpts.add(rcpt);
        }

        return rcpts;
    }

    /**
     * Sets recipients addresses.
     *
     * @param rcpts Recipients address list.
     * @return Self.
     */
    public MessageEnvelope setRcpts(List<String> rcpts) {
        this.rcpts = rcpts;
        return this;
    }

    /**
     * Add SMTP parameters.
     *
     * @param extension SMTP extension to add parameter too.
     * @param param     Param and value if any.
     * @return Self.
     */
    public MessageEnvelope addParam(String extension, String param) {
        if (params.containsKey("extension")) {
            params.put(extension, Stream.concat(params.get(extension).stream(), Stream.of(param))
                    .collect(Collectors.toList()));
        } else {
            params.put(extension.toLowerCase(), Stream.of(param).collect(Collectors.toList()));
        }
        return this;
    }

    /**
     * Gets SMTP parameter by name.
     *
     * @param extension SMTP extension to get parameter for.
     * @return String.
     */
    public String getParams(String extension) {
        List<String> param = params.get(extension);
        return param != null && !param.isEmpty() ? " " + String.join(" ", param) : "";
    }

    /**
     * Adds magic header.
     *
     * @param name  Header name.
     * @param value Header value.
     * @return Self.
     */
    public MessageEnvelope addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Sets magic headers.
     *
     * @param headers Map of String, String.
     * @return Self.
     */
    public MessageEnvelope setHeaders(Map<String, String> headers) {
        this.headers.clear();
        this.headers.putAll(headers);
        return this;
    }

    /**
     * Gets magic headers.
     *
     * @return Map of String, String.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Is prepend headers.
     * <p>Auto prepend all magic headers to the email.
     *
     * @return Boolean.
     */
    public boolean isPrependHeaders() {
        return prependHeaders;
    }

    /**
     * Sets prepend headers.
     *
     * @param prependHeaders Boolean.
     * @return Self.
     */
    public MessageEnvelope setPrependHeaders(boolean prependHeaders) {
        this.prependHeaders = prependHeaders;
        return this;
    }

    /**
     * Gets path to eml file.
     *
     * @return File path.
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets path to eml file.
     *
     * @param file File path.
     * @return Self.
     */
    public MessageEnvelope setFile(String file) {
        this.file = file;
        return this;
    }

    /**
     * Gets path to folder.
     *
     * @return folder path.
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Sets path to folder.
     *
     * @param folder Folder path.
     * @return Self.
     */
    public MessageEnvelope setFolder(String folder) {
        this.folder = folder;
        return this;
    }

    /**
     * Gets file path to eml file from folder contents if any.
     *
     * @return Folder path.
     */
    public String getFolderFile() {
        return PathUtils.folderFile(getFolder(), Collections.singletonList("eml"));
    }

    /**
     * Gets MimeConfig.
     *
     * @return MimeConfig instance.
     */
    public MimeConfig getMime() {
        return mime;
    }

    /**
     * Sets MimeConfig.
     *
     * @param mime MimeConfig instance.
     * @return Self.
     */
    public MessageEnvelope setMime(MimeConfig mime) {
        this.mime = mime;
        return this;
    }

    /**
     * Gets eml stream.
     *
     * @return Eml stream.
     */
    public InputStream getStream() {
        return stream;
    }

    /**
     * Sets eml stream.
     *
     * @param stream Eml stream.
     * @return Self.
     */
    public MessageEnvelope setStream(InputStream stream) {
        this.stream = stream;
        return this;
    }

    /**
     * Gets subject.
     *
     * @return Subject string.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets subject.
     * <p>May be used if no eml file provided.
     * <p>Basic plain/text eml will be generated.
     *
     * @param subject Subject string.
     * @return Self.
     */
    public MessageEnvelope setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Gets message.
     * <p>May be used if no eml file provided.
     * <p>Basic plain/text eml will be generated.
     *
     * @return Message string.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets email body.
     *
     * @param message Body string.
     * @return Self.
     */
    public MessageEnvelope setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Builds email headers.
     *
     * @return Email headers string.
     */
    public String buildHeaders() {
        String to = "<" + String.join(">, <", getRcpts()) + ">";

        return "MIME-Version: 1.0\r\n" +
                "Message-ID: <" + msgId + mail + ">\r\n" +
                "Date: " + date + "\r\n" +
                "From: <" + mail + ">\r\n" +
                "To: " + to + "\r\n" +
                "Subject: " + (subject != null ? subject : "") + "\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Transfer-Encoding: 8bit\r\n";
    }

    /**
     * Gets chunk size.
     * <p>Size of how many bytes to write to the socket in one write.
     *
     * @return Chunk size in bytes.
     */
    public int getChunkSize() {
        return chunkSize;
    }

    /**
     * Sets chunk size.
     *
     * @param chunkSize Chunk size.
     * @return Self.
     */
    public MessageEnvelope setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }

    /**
     * Is chunk BDAT command.
     * <p>This makes the client write the BDAT command with the first chunk of the message.
     * <p>This can uncover accidental buffer clearing when switching from SMTP to MIME mode.
     *
     * @return Boolean.
     */
    public boolean isChunkBdat() {
        return chunkBdat;
    }

    /**
     * Sets chunk BDAT command.
     *
     * @param chunkBdat Boolean.
     * @return Self.
     */
    public MessageEnvelope setChunkBdat(boolean chunkBdat) {
        this.chunkBdat = chunkBdat;
        return this;
    }

    /**
     * Is chunk write randomly.
     * <p>This will ignore chunk size and just write random size chunks.
     * <p>The chunks are limite to in between 1024 and 2048 bytes.
     *
     * @return Boolean.
     */
    public boolean isChunkWrite() {
        return chunkWrite;
    }

    /**
     * Sets chunk write.
     *
     * @param chunkWrite Boolean.
     * @return Self.
     */
    public MessageEnvelope setChunkWrite(boolean chunkWrite) {
        this.chunkWrite = chunkWrite;
        return this;
    }

    /**
     * Gets terminate after bytes.
     * <p>Size of how many bytes to write to the socket before terminating connection.
     *
     * @return Size in bytes.
     */
    public int getTerminateAfterBytes() {
        return terminateAfterBytes;
    }

    /**
     * Sets chunk size.
     *
     * @param terminateAfterBytes Size in bytes.
     * @return Self.
     */
    public MessageEnvelope setTerminateAfterBytes(int terminateAfterBytes) {
        this.terminateAfterBytes = terminateAfterBytes;
        return this;
    }

    /**
     * Is terminate before dot.
     * <p>Terminate connection before transmitting the &lt;CRLF&gt;.&lt;CRLF&gt; termiantor.
     *
     * @return Boolean.
     */
    public boolean isTerminateBeforeDot() {
        return terminateBeforeDot;
    }

    /**
     * Sets terminate after dot.
     *
     * @param terminateBeforeDot Boolean.
     * @return Self.
     */
    public MessageEnvelope setTerminateBeforeDot(boolean terminateBeforeDot) {
        this.terminateBeforeDot = terminateBeforeDot;
        return this;
    }

    /**
     * Is terminate after dot.
     * <p>Terminate connection after transmitting the &lt;CRLF&gt;.&lt;CRLF&gt; termiantor.
     *
     * @return Boolean.
     */
    public boolean isTerminateAfterDot() {
        return terminateAfterDot;
    }

    /**
     * Sets terminate after dot.
     *
     * @param terminateAfterDot Boolean.
     * @return Self.
     */
    public MessageEnvelope setTerminateAfterDot(boolean terminateAfterDot) {
        this.terminateAfterDot = terminateAfterDot;
        return this;
    }

    /**
     * Gets slow bytes.
     * <p>This adds a write delay every given number of bytes.
     * <p>Must be &gt;= 128 or the functionality will be disabled.
     * <p>Works only with file and stream not with headers and message.
     *
     * @return Size in bytes.
     */
    public int getSlowBytes() {
        return slowBytes;
    }

    /**
     * Sets slow bytes.
     *
     * @param slowBytes Chunk size.
     * @return Self.
     */
    public MessageEnvelope setSlowBytes(int slowBytes) {
        this.slowBytes = slowBytes;
        return this;
    }

    /**
     * Gets slow wait.
     * <p>Wait time in miliseconds.
     * <p>Must be &gt;= 100 or the functionality will be disabled.
     *
     * @return Chunk size in bytes.
     */
    public int getSlowWait() {
        return slowWait;
    }

    /**
     * Sets slow wait.
     *
     * @param slowWait Slow wait.
     * @return Self.
     */
    public MessageEnvelope setSlowWait(int slowWait) {
        this.slowWait = slowWait;
        return this;
    }

    /**
     * Gets repeat times.
     * <p>Send the same envelope this many times +1.
     *
     * @return Repeat times.
     */
    public int getRepeat() {
        return repeat;
    }

    /**
     * Sets repeat times.
     *
     * @param repeat Repeat times.
     * @return Self.
     */
    public MessageEnvelope setRepeat(int repeat) {
        this.repeat = repeat;
        return this;
    }

    /**
     * Gets AssertConfig.
     *
     * @return AssertConfig instance.
     */
    public AssertConfig getAssertions() {
        return assertConfig;
    }

    /**
     * Sets AssertConfig.
     *
     * @param assertConfig AssertConfig instance.
     * @return MessageEnvelope instance.
     */
    public MessageEnvelope setAssertions(AssertConfig assertConfig) {
        this.assertConfig = assertConfig;
        return this;
    }
}

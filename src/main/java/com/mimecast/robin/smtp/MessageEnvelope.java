package com.mimecast.robin.smtp;

import com.mimecast.robin.config.assertion.AssertConfig;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Message envelope.
 *
 * <p>This is the container for SMTP envelopes.
 * <p>It will store the meta data associated with each email sent.
 */
public class MessageEnvelope {

    // Set MAIL FROM and RCPT TO.
    private String mail = null;
    private String mailEjf = null;
    private String rcpt = null;
    private String rcptEjf = null;
    private List<String> rcpts = new ArrayList<>();

    // Set EML file or null.
    private String file = null;

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

    // Assertions to be made against transaction.
    private AssertConfig assertConfig;

    /**
     * Constructs a new MessageEnvelope instance.
     */
    public MessageEnvelope() {
        date = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z", Locale.UK).format(new Date());
        String now = String.valueOf(System.currentTimeMillis());
        String uid = UUID.randomUUID().toString() + "-" + now;

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
     * Gets secondary MAIL FROM address for EJF cases.
     *
     * @return MAIL FROM address.
     */
    public String getMailEjfFrom() {
        if (StringUtils.isNotBlank(mailEjf)) {
            return mailEjf;
        }
        return "";
    }

    /**
     * Gets secondary RCPT TO address for EJF cases.
     *
     * @return RCPT TO address.
     */
    public String getRcptEjfTo() {
        if (StringUtils.isNotBlank(rcptEjf)) {
            return rcptEjf;
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
     */
    public void setMail(String mail) {
        this.mail = mail;
    }

    /**
     * Gets MAIL for EJF.
     *
     * @return MAIL address.
     */
    public String getMailEjf() {
        return mailEjf;
    }

    /**
     * Sets MAIL for EJF.
     *
     * @param mailEjf MAIL address.
     */
    public void setMailEjf(String mailEjf) {
        this.mailEjf = mailEjf;
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
     */
    public void setRcpt(String rcpt) {
        this.rcpt = rcpt;
    }

    /**
     * Gets RCPT for EJF.
     *
     * @return RCPT address.
     */
    public String getRcptEjf() {
        return rcptEjf;
    }

    /**
     * Sets RCPT for EJF.
     *
     * @param rcptEjf RCPT address.
     */
    public void setRcptEjf(String rcptEjf) {
        this.rcptEjf = rcptEjf;
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
     */
    public void setRcpts(List<String> rcpts) {
        this.rcpts = rcpts;
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
     */
    public void setFile(String file) {
        this.file = file;
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
     */
    public void setStream(InputStream stream) {
        this.stream = stream;
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
     */
    public void setSubject(String subject) {
        this.subject = subject;
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
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets email headers.
     *
     * @return Email headers string.
     */
    public String getHeaders() {
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
     */
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
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
     */
    public void setChunkBdat(boolean chunkBdat) {
        this.chunkBdat = chunkBdat;
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
     */
    public void setChunkWrite(boolean chunkWrite) {
        this.chunkWrite = chunkWrite;
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
     */
    public void setTerminateAfterBytes(int terminateAfterBytes) {
        this.terminateAfterBytes = terminateAfterBytes;
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
     */
    public void setTerminateBeforeDot(boolean terminateBeforeDot) {
        this.terminateBeforeDot = terminateBeforeDot;
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
     */
    public void setTerminateAfterDot(boolean terminateAfterDot) {
        this.terminateAfterDot = terminateAfterDot;
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
     */
    public void setSlowBytes(int slowBytes) {
        this.slowBytes = slowBytes;
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
     */
    public void setSlowWait(int slowWait) {
        this.slowWait = slowWait;
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
     */
    public void setAssertions(AssertConfig assertConfig) {
        this.assertConfig = assertConfig;
    }
}

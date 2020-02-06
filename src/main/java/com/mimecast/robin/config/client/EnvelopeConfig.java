package com.mimecast.robin.config.client;

import com.mimecast.robin.config.assertion.AssertConfig;
import com.mimecast.robin.config.ConfigFoundation;
import com.mimecast.robin.smtp.MessageEnvelope;

import java.util.List;
import java.util.Map;

/**
 * Case envelope configuration container.
 * <p>This is a container for envelopes defined in a case configuration.
 * <p>One instance will be made for every envelope defined.
 * <p>This will be used to collect the necessary data to generate the MessageEnvelope.
 *
 * @see CaseConfig
 * @see MessageEnvelope
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
@SuppressWarnings("unchecked")
public class EnvelopeConfig extends ConfigFoundation {

    /**
     * Constructs a new EnvelopeConfig instance with configuration map.
     *
     * @param map Envelope map.
     */
    @SuppressWarnings("rawtypes")
    public EnvelopeConfig(Map map) {
        super(map);
    }

    /**
     * Gets MAIL FROM.
     *
     * @return MAIL FROM address.
     */
    public String getMail() {
        return getStringProperty("mail");
    }

    /**
     * Gets RCPT TO.
     *
     * @return RCPT TO address.
     */
    public List<String> getRcpt() {
        return getListProperty("rcpt");
    }

    /**
     * Gets secondary MAIL FROM address for EJF cases.
     *
     * @return MAIL FROM address.
     */
    public String getMailEjf() {
        return getStringProperty("mailEjf");
    }

    /**
     * Gets secondary RCPT TO address for EJF cases.
     *
     * @return RCPT TO address.
     */
    public String getRcptEjf() {
        return getStringProperty("rcptEjf");
    }

    /**
     * Gets chunk size.
     * <p>Size of how many bytes to write to the socket in one write.
     *
     * @return Chunk size in bytes.
     */
    public int getChunkSize() {
        return Math.toIntExact(getLongProperty("chunkSize"));
    }

    /**
     * Is chunk BDAT command.
     * <p>This makes the client write the BDAT command with the first chunk of the message.
     * <p>This can uncover accidental buffer clearing when switching from SMTP to MIME mode.
     *
     * @return Boolean.
     */
    public boolean isChunkBdat() {
        return getBooleanProperty("chunkBdat");
    }

    /**
     * Is chunk write randomly.
     * <p>This will write random size chunks from chunk size chunks.
     * <p>The chunks are limited to in between 1024 and 2048 bytes so chunk size must be >= to 2048.
     *
     * @return Boolean.
     */
    public boolean isChunkWrite() {
        return getBooleanProperty("chunkWrite");
    }

    /**
     * Gets terminate after bytes.
     * <p>Size of how many bytes to write to the socket before terminating connection.
     *
     * @return Size in bytes.
     */
    public int getTerminateAfterBytes() {
        return Math.toIntExact(getLongProperty("terminateAfterBytes"));
    }

    /**
     * Is terminate before dot.
     * <p>Terminate connection before transmitting the &lt;CRLF&gt;.&lt;CRLF&gt; termiantor.
     *
     * @return Boolean.
     */
    public boolean isTerminateBeforeDot() {
        return getBooleanProperty("terminateBeforeDot");
    }

    /**
     * Is terminate after dot.
     * <p>Terminate connection after transmitting the &lt;CRLF&gt;.&lt;CRLF&gt; termiantor.
     *
     * @return Boolean.
     */
    public boolean isTerminateAfterDot() {
        return getBooleanProperty("terminateAfterDot");
    }

    /**
     * Gets slow bytes.
     * <p>This adds a write delay every given number of bytes.
     *
     * @return Size in bytes.
     */
    public int getSlowBytes() {
        return Math.toIntExact(getLongProperty("slowBytes"));
    }

    /**
     * Gets slow wait.
     *
     * @return Time in miliseconds.
     */
    public int getSlowWait() {
        return Math.toIntExact(getLongProperty("slowWait"));
    }

    /**
     * Gets path to eml file.
     *
     * @return File path.
     */
    public String getFile() {
        return getStringProperty("file");
    }

    /**
     * Gets subject.
     * <p>May be used if no eml file provided.
     * <p>Basic plain/text eml will be generated.
     *
     * @return Subject string.
     */
    public String getSubject() {
        return getStringProperty("subject");
    }

    /**
     * Gets message.
     * <p>May be used if no eml file provided.
     * <p>Basic plain/text eml will be generated.
     *
     * @return Message string.
     */
    public String getMessage() {
        return getStringProperty("message");
    }

    /**
     * Gets assertions.
     *
     * @return Assertions map.
     */
    public AssertConfig getAssertions() {
        return new AssertConfig(getMapProperty("assertions"));
    }
}

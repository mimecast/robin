package com.mimecast.robin.config;

import com.mimecast.robin.config.client.CaseConfig;
import com.mimecast.robin.config.client.EnvelopeConfig;
import com.mimecast.robin.main.Config;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.session.Session;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Mapper of CaseConfig to Session.
 */
public class ConfigMapper {

    /**
     * Config.
     */
    protected final CaseConfig config;

    /**
     * Mapper for CaseConfig to Session.
     *
     * @param config CaseConfig instance.
     */
    public ConfigMapper(CaseConfig config) {
        this.config = config;
    }

    /**
     * Map configuration to given Session.
     *
     * @param session Session instance.
     */
    public void mapTo(Session session) {
        // Set MTA and PORT.
        session.setMx(config.getMx())
                .setRetry(config.getRetry())
                .setDelay(config.getDelay())
                .setTimeout(config.getTimeout())
                .setPort(config.getPort())
                .setTls(config.isTls())
                .setAuthBeforeTls(config.isAuthBeforeTls())
                .setProtocols(config.getProtocols())
                .setCiphers(config.getCiphers());

        // Set EHLO domain.
        if (StringUtils.isNotBlank(config.getEhlo())) {
            session.setEhlo(config.getEhlo());
        }

        // Auth.
        if (config.isAuth()) {
            session.setAuth(true)
                    .setAuthLoginCombined(config.isAuthLoginCombined())
                    .setAuthLoginRetry(config.isAuthLoginRetry())
                    .setUsername(config.getUser())
                    .setPassword(config.getPass());
        }

        // Add assertion config.
        session.addAssertions(config.getAssertions());

        for (EnvelopeConfig envelope : config.getEnvelopes()) {
            addEnvelope(session, envelope, config);
        }
    }

    /**
     * Add envelope.
     *
     * @param session        Session instance.
     * @param envelopeConfig EnvelopeConfig instance.
     * @param caseConfig     CaseCOnfig instance.
     */
    private void addEnvelope(Session session, EnvelopeConfig envelopeConfig, CaseConfig caseConfig) {
        // Message object.
        MessageEnvelope envelope = new MessageEnvelope()
            .setAssertions(envelopeConfig.getAssertions());

        // Set MAIL FROM and RCPT TO.
        envelope.setMail(envelopeConfig.getMail() != null ? magicReplace(envelopeConfig.getMail()) : caseConfig.getMail());
        List<String> rcpts = !envelopeConfig.getRcpt().isEmpty() ? envelopeConfig.getRcpt() : caseConfig.getRcpt();
        for (String rcpt : rcpts) {
            envelope.getRcpts().add(magicReplace(rcpt));
        }

        // EJF.
        envelope.setMailEjf(magicReplace(envelopeConfig.getMailEjf()))
            .setRcptEjf(magicReplace(envelopeConfig.getRcptEjf()));

        // Transfer config.
        if (envelopeConfig.getChunkSize() > 128) {
            envelope.setChunkSize(envelopeConfig.getChunkSize());
        }
        envelope.setChunkBdat(envelopeConfig.isChunkBdat())
            .setChunkWrite(envelopeConfig.isChunkWrite())

            .setTerminateAfterBytes(envelopeConfig.getTerminateAfterBytes())
            .setTerminateBeforeDot(envelopeConfig.isTerminateBeforeDot())
            .setTerminateAfterDot(envelopeConfig.isTerminateAfterDot())

            .setSlowBytes(envelopeConfig.getSlowBytes())
            .setSlowWait(envelopeConfig.getSlowWait());

        // Set MIME.
        if (!envelopeConfig.getMime().isEmpty()) {
            envelope.setMime(envelopeConfig.getMime());

            // Add message to delivery.
            session.addEnvelope(envelope);
        }

        // Set EML file.
        else if (StringUtils.isNotBlank(envelopeConfig.getFile())) {
            envelope.setFile(envelopeConfig.getFile());

            // Add message to delivery.
            session.addEnvelope(envelope);
        }

        // If EML is null set subject and message.
        else if (StringUtils.isNotBlank(envelopeConfig.getSubject()) && StringUtils.isNotBlank(envelopeConfig.getMessage())) {
            envelope.setSubject(envelopeConfig.getSubject())
                .setMessage(envelopeConfig.getMessage());

            // Add message to delivery.
            session.addEnvelope(envelope);
        }
    }

    /**
     * Replace magic configuration variables.
     *
     * @param variable Variable string.
     * @return Original string or replaced.
     */
    private String magicReplace(String variable) {
        String ret = variable;

        if (StringUtils.isNotBlank(variable) && variable.startsWith("{$")) {
            switch (variable) {
                case "{$mail}":
                    ret = Config.getClient().getMail();
                    break;

                case "{$rcpt}":
                    if (Config.getClient().getRcpt() != null && !Config.getClient().getRcpt().isEmpty()) {
                        ret = Config.getClient().getRcpt().get(0);
                    }
                    break;

                default:
                    ret = "";
                    break;
            }
        }

        return ret;
    }
}

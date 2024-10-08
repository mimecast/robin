package com.mimecast.robin.config;

import com.mimecast.robin.config.client.CaseConfig;
import com.mimecast.robin.config.client.EnvelopeConfig;
import com.mimecast.robin.main.Config;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.session.Session;
import com.mimecast.robin.util.Magic;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

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
        // Repeat
        List<Map<String, Object>> envelopes = new LinkedList<>();
        for (EnvelopeConfig envelopeConfig : config.getEnvelopes()) {
            envelopes.add(envelopeConfig.getMap()); // Add original envelope.

            // Add repeat envelope copies.
            for (int i = 0; i < envelopeConfig.getRepeat(); i++) {
                envelopes.add(envelopeConfig.copy().getMap());
            }
        }
        config.getMap().put("envelopes", envelopes);

        addMagic(session);

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

        // Add behaviour config.
        session.setBehaviour(config.getBehaviour());

        // Add assertion config.
        session.addAssertions(config.getAssertions());

        for (EnvelopeConfig envelope : config.getEnvelopes()) {
            addEnvelope(session, envelope, config);
        }
    }

    /**
     * Add magic variables.
     *
     * @param session Session instance.
     */
    @SuppressWarnings("unchecked")
    private void addMagic(Session session) {
        if (config.hasProperty("$")) {
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) config.getMapProperty("$")).entrySet()) {
                session.putMagic(
                        entry.getKey(),
                        entry.getValue() instanceof String ?
                                Magic.magicReplace((String) entry.getValue(), session) :
                                entry.getValue()
                );
            }
        }

        if (config.hasProperty("route")) {
            config.getMap().put("route", Magic.magicReplace(config.getStringProperty("route"), session));
        }

        if (config.hasProperty("envelopes")) {
            for (Object envelope : config.getListProperty("envelopes")) {
                if (envelope instanceof Map) {
                    Map<String, Object> envelopeMap = (Map<String, Object>) envelope;
                    if (envelopeMap.containsKey("mail")) {
                        envelopeMap.put("mail", Magic.magicReplace((String) envelopeMap.get("mail"), session));
                    }
                    if (envelopeMap.containsKey("rcpt")) {
                        List<String> rcptList = new ArrayList<>();
                        for (String address : (List<String>) envelopeMap.get("rcpt")) {
                            rcptList.add(Magic.magicReplace(address, session));
                        }
                        envelopeMap.put("rcpt", rcptList);
                    }
                    if (envelopeMap.containsKey("params")) {
                        Map<String, List<String>> params = new HashMap<>();
                        for (Map.Entry<String, List<String>> param : ((Map<String, List<String>>) envelopeMap.get("params")).entrySet()) {
                            params.put(param.getKey(), param.getValue().stream().map(e -> e = Magic.magicReplace(e, session)).collect(Collectors.toList()));
                        }
                        envelopeMap.put("params", params);
                    }
                }
            }
        }
    }

    /**
     * Add envelope.
     *
     * @param session        Session instance.
     * @param envelopeConfig EnvelopeConfig instance.
     * @param caseConfig     CaseCOnfig instance.
     */
    @SuppressWarnings("unchecked")
    private void addEnvelope(Session session, EnvelopeConfig envelopeConfig, CaseConfig caseConfig) {
        // Message object.
        MessageEnvelope envelope = new MessageEnvelope()
                .setAssertions(envelopeConfig.getAssertions());

        // Set MAIL FROM and RCPT TO.
        envelope.setMail(envelopeConfig.getMail() != null ? magicReplace(envelopeConfig.getMail(), session) : caseConfig.getMail());
        List<String> rcpts = !envelopeConfig.getRcpt().isEmpty() ? envelopeConfig.getRcpt() : caseConfig.getRcpt();
        for (String rcpt : rcpts) {
            envelope.getRcpts().add(magicReplace(rcpt, session));
        }

        // Magic params.
        for (Map.Entry<String, List<String>> param : envelopeConfig.getParams().entrySet()) {
            param.getValue().forEach(e -> envelope.addParam(param.getKey().toLowerCase(), magicReplace(e, session)));
        }

        // Magic headers.
        envelopeConfig.getHeaders().forEach((k, v) -> {
            if (v instanceof String) {
                envelope.addHeader(k, magicReplace((String) v, session));
            } else if (v instanceof List) {
                envelope.addHeader(k, ((List<String>) v).stream().map(s -> magicReplace(s, session)).collect(Collectors.joining("; ")));
            }
        });

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
                .setSlowWait(envelopeConfig.getSlowWait())
                .setRepeat(envelopeConfig.getRepeat())
                .setPrependHeaders(envelopeConfig.isPrependHeaders());

        // Set MIME.
        if (!envelopeConfig.getMime().isEmpty()) {
            envelope.setMime(envelopeConfig.getMime());

            // Add message to delivery.
            session.addEnvelope(envelope);
        }

        // Set EML file.
        else if (StringUtils.isNotBlank(envelopeConfig.getFile())) {
            envelope.setFile(magicReplace(envelopeConfig.getFile(), session));

            // Add message to delivery.
            session.addEnvelope(envelope);
        }

        // Set ramdom EML file from folder.
        else if (StringUtils.isNotBlank(envelopeConfig.getFolder())) {
            envelope.setFolder(magicReplace(envelopeConfig.getFolder(), session));

            // Add message to delivery.
            session.addEnvelope(envelope);
        }

        // If EML is null set subject and message.
        else if (StringUtils.isNotBlank(envelopeConfig.getSubject()) && StringUtils.isNotBlank(envelopeConfig.getMessage())) {
            envelope.setSubject(magicReplace(envelopeConfig.getSubject(), session))
                    .setMessage(magicReplace(envelopeConfig.getMessage(), session));

            // Add message to delivery.
            session.addEnvelope(envelope);
        }
    }

    /**
     * Replace magic configuration variables.
     *
     * @param variable Variable string.
     * @param session Session instance.
     * @return Original string or replaced.
     */
    protected String magicReplace(String variable, Session session) {
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

        return Magic.magicReplace(ret, session);
    }
}

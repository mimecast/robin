package com.mimecast.robin.smtp.verb;

/**
 * EHLO verb.
 * <p>This is used for parsing HELO/EHLO commands.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class EhloVerb extends Verb {

    /**
     * Constructs a new EhloVerb instance with given Verb.
     *
     * @param verb Verb instance.
     */
    public EhloVerb(Verb verb) {
        super(verb);
    }

    /**
     * Gets EHLO domain.
     *
     * @return Domain.
     */
    public String getDomain() {
        return getCount() > 1 ? getPart(1) : "";
    }
}

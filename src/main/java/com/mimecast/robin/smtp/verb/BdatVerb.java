package com.mimecast.robin.smtp.verb;

/**
 * BDAT verb.
 * <p>This is used for parsing BDAT commands for CHUNKING implementation.
 * @link https://tools.ietf.org/html/rfc3030 RFC3030
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class BdatVerb extends Verb {

    /**
     * BDAT size.
     */
    private int size = 0;

    /**
     * BDAT LAST.
     */
    private boolean last = false;

    /**
     * Constructs a new BdatVerb instance with given Verb.
     *
     * @param verb Verb instance.
     */
    public BdatVerb(Verb verb) {
        super(verb);
    }

    /**
     * Gets BDAT size.
     *
     * @return Size.
     */
    public int getSize() {
        if (size == 0) {
            size = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        }

        return size;
    }

    /**
     * Is BDAT last.
     *
     * @return True if found.
     */
    public boolean isLast() {
        if (!last) {
            last = parts.length > 2 && parts[2].equalsIgnoreCase("last");
        }

        return last;
    }
}

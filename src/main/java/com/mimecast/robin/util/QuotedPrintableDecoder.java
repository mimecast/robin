package com.mimecast.robin.util;

import org.apache.commons.codec.DecoderException;

import java.io.ByteArrayOutputStream;

/**
 * Quited printable decoding Apache commons altered decoder utility.
 */
public class QuotedPrintableDecoder {

    /**
     * Characters.
     */
    private static final byte ESCAPE_CHAR = '=';
    private static final byte CR = 13;
    private static final byte LF = 10;

    /**
     * Copy of: org.apache.commons.codec.net.QuotedPrintableCodec
     * <p>Fixed to preserve line endings.
     * <p>Decodes an array quoted-printable characters into an array of original bytes. Escaped characters are converted
     * back to their original representation.
     * <p>This function fully implements the quoted-printable encoding specification (rule #1 through rule #5) as
     * defined in RFC 1521.
     *
     * @param bytes array of quoted-printable characters
     * @return array of original bytes
     * @throws DecoderException Thrown if quoted-printable decoding is unsuccessful
     */
    public static byte[] decode(final byte[] bytes) throws DecoderException {
        if (bytes == null) {
            return null;
        }
        boolean skipLine = false;
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < bytes.length; i++) {
            final int b = bytes[i];
            if (b == ESCAPE_CHAR) {
                try {
                    if (bytes[++i] == CR) {
                        skipLine = true;
                        continue;
                    }
                    final int u = digit16(bytes[i]);
                    final int l = digit16(bytes[++i]);
                    buffer.write((char) ((u << 4) + l));
                } catch (final ArrayIndexOutOfBoundsException e) {
                    throw new DecoderException("Invalid quoted-printable encoding", e);
                }
            } else if ((b != CR && b != LF) || !skipLine) {
                skipLine = false;
                buffer.write(b);
            }
        }
        return buffer.toByteArray();
    }

    /**
     * Radix used in encoding and decoding.
     */
    private static final int RADIX = 16;

    /**
     * Copy of: org.apache.commons.codec.net.Utils
     * <p>Returns the numeric value of the character <code>b</code> in radix 16.
     *
     * @param b The byte to be converted.
     * @return The numeric value represented by the character in radix 16.
     * @throws DecoderException Thrown when the byte is not valid per {@link Character#digit(char, int)}
     */
    static int digit16(final byte b) throws DecoderException {
        final int i = Character.digit((char) b, RADIX);
        if (i == -1) {
            throw new DecoderException("Invalid URL encoding: not a valid digit (radix " + RADIX + "): " + b);
        }
        return i;
    }
}

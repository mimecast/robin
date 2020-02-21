/**
 * SMTP transactions containers.
 *
 * <p>Every SMTP exchange is a transaction that gets recorded in it's place.
 * <br>These can be at session level or envelope level.
 * <br>Session reffers to the overall connection while envelope strictly to each message sent.
 * <br>Envelope SMTP extensions are: MAIL, RCPT, DATA, BDAT (also known as CHUNKING extension).
 */
package com.mimecast.robin.smtp.transaction;

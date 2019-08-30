Magic variables
===============

Email (.eml) files may contain these magic variables.
Use these to program your emails to autocomplete information.

- **{$DATE}** - RFC compliant current date.
- **{$MSGID}** - Random string. Combines with {$MAILFROM} to form a valid Message-ID.
- **{$MAILFROM}** - Envelope mail address.
- **{$RCPTTO}** - Envelope rcpt address.
- **{$MAILEJFFROM}** - Envelope mailEjf address.
- **{$RCPTEJFTO}** - Envelope rcptEjf address.
- **{$RANDNO}** - Random number between 1 and 10.
- **{$RANDCH}** - Random 20 alpha characters.
- **{$RANDNO#}** - Generates random number of given length (example: **{$RANDNO3}**).
- **{$RANDCH#}** - Random alpha characters of given length (example: **{$RANDCH15}**).

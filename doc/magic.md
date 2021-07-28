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


Magic headers
=============

The following headers will enable additional functionalities within the Robin server component uppon receipt.

- **X-Robin-Filename** - If a value is present and valid filename, this will be used to rename the stored eml file. 
- **X-Robin-Relay** - If a value is present and valid server name and optional port number emai will be relayed to it post receipt.

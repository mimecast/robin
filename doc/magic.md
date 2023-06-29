Magic client
============

Case files inherit the default config from client.json5.
However, in some cases you may want to use the defaults in some cases.
For those cases you may use the following magic variables in case files.

- `{$mail}` - This always references the client.json5 mail param value if any.
- `{$rcpt}` - This always references the client.json5 rcpt param first value if any.


Magic session
=============

The Session object has a magic store where it loads up CLI params, properties file data and data from external assertions.
It can also be seeded using a `$` variable in the case file like so:

    $: {
        fromUser: "robin",
        fromDomain: "example.com",
        fromAddress: "{$fromUser}@{$fromDomain}",
        toUser: "lady",
        toDomain: "example.com",
        toAddress: "{$toUser}@{$toDomain}",
    }

Lastly the session also contains the following:
- `{$uid}` - The Session uid used in logging and storage file path.
- `{$yymd}` - Date in `yyyyMMdd` format and storage file path.

All of these can be used throught the case files to aid testing automation.


Magic eml
=========

Email (.eml) files may contain these magic variables.
Use these to program your emails to autocomplete information.

- `{$DATE}` - RFC compliant current date.
- `{$YYMD}` - YYYYMMDD date.
- `{$MSGID}` - Random string. Combines with {$MAILFROM} to form a valid Message-ID.
- `{$MAILFROM}` - Envelope mail address.
- `{$MAIL}` - Envelope mail address.
- `{$RCPTTO}` - Envelope rcpt address.
- `{$RCPT}` - Envelope rcpt address.
- `{$HEADERS}` - Magic headers.
- `{$HEADERS[*]}` - Magic header by name.
- `{$RANDNO}` - Random number between 1 and 10.
- `{$RANDCH}` - Random 20 alpha characters.
- `{$RANDNO#}` - Generates random number of given length (example: `{$RANDNO3}`).
- `{$RANDCH#}` - Random alpha characters of given length (example: `{$RANDCH15}`).
- `{$HEADERS}` - Add all custom headers.
- `{$HEADERS[#]}` - Add header value by key (example: `{$HEADERS[FROM]}`).


Magic eml headers
=================

The following headers will enable additional functionalities within the Robin server component uppon receipt.

- `X-Robin-Filename` - If a value is present and valid filename, this will be used to rename the stored eml file.
- `X-Robin-Relay` - If a value is present and valid server name and optional port number emai will be relayed to it post receipt.

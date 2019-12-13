Test Cases
==========
A framework for writing tests for automation.


Glossary
--------

### Session
- **retry** - Retry connection.
- **delay** - Delay between retries.
- **mx** - MTA server list (default: 127.0.0.1).
- **port** - MTA port (default: 25).
- **auth** - SMTP authentication (default: false).
- **user** - Authentication username.
- **pass** - Authentication password.
- **route** - Selects a preconfigured route (see: client.json for routes configuration).
- **tls** - Allows the client to do TLS when STARTTLS is advertised (default: true).
- **protocols** - TLS protocols to support (default: SSLv3, SSLv2Hello, TLSv1, TLSv1.1, TLSv1.2).
- **authTls**  - Send AUTH before STARTLS (default: false). Vulnerable behaviour.
- **authLoginCombined**  - Send username and password in one line for AUTH LOGIN.
- **authLoginRetry**  - Disable authLoginCombined and retry AUTH LOGIN.
- **ehlo** - Ehlo domain (default: hostname).


#### XCLIENT
- **xclient** - Special feature allowing enumation of client info:
  - addr - IP address.
  - name - Reverse DNS.
  - helo - HELO/EHLO domain.


### Envelope
- **mail** - Sender (default: client.json value).
- **rcpt** - Recipient (default: client.json value).
- **mailEjf** - Special variable for EJF magic. Value {$mail} will use client.json mail value.
- **rcptEjf** - Special variable for EJF magic. Value {$rcpt} will use client.json rcpt value.
- **chunkSize** - (default: 1400 bytes) Enables CHUNKING when set at minimum 128.
- **chunkBdat**  - (default: false) Includes the BDAT command along with the first chunk.
- **chunkWrite** - (default: false) Writes email data to socket in uneven chunks between 1024 and 2048 bytes if chunkSize greater than 2048.
- **slowBytes** - (default: 1) Adds a write delay every given number of bytes if value is greater of equal to 1.
- **slowWait** - (default: 0) Wait time in miliseconds if grater or equal to 100.


#### Message
Every message should have either a file or a subject/message pair!
If a file is not defined a MIME source will be generated from envelope date along with subject and message.
Shile message is mandatory subject may be left blank.

- **file** - Path to eml file to be transmitted.
- **subject** - Email subject.
- **message** - Email text/plain message.


SMTP Assertions
---------------
Regex assertions to be run against SMTP transactions.
There are session level and envelope level assertions.

#### Session
- SMTP - Initial connection response.
- EHLO
- STARTTLS
- TLS - Successfull handshake protocol / cipher.
- SHLO - Post STARTLS EHLO response.
- XCLIENT
- XHLO - Post XCLIENT EHLO response.
- AUTH
- RSET
- HELP
- QUIT

        [ "SMTP", "^220" ],
        [ "EHLO", "STARTTLS" ],
        [ "STARTTLS", "^220" ],
        [ "TLS", "^TLSv1.2:TLS_RSA_WITH_AES_256_GCM_SHA384" ],
        [ "SHLO", "250 HELP" ]
        [ "AUTH", "^235" ]
        [ "QUIT", "^221" ]


#### Envelope
- MAIL
- RCPT
- DATA/BDAT - Mutully exclusive.

        [ "MAIL", "^250" ],
        [ "RCPT", "^250" ],
        [ "DATA", "^250" ],
        [ "DATA", "Received OK$" ],
        [ "BDAT", "^250" ],


MTA Assertions
---------------
You may run assertions against your MTA logs from envelope level.
A logs client needs to be plugged in for this functionality to work.
LogsClient.java interface.

- **wait** - Initial wait before calling the logs client (default: 2, min: 2).
- **retry** - Retry attempts if empty logs list (default: 0).
- **delay** - Delay between subsequent retries (default: 2, min: 2).
- **verify** - List of regex matches to verify complete logs received. Provides stability when MTA takes more time to process.
- **match** - Regex assertions to run against log lines. Multiple expressions can run on the same line. All must match.
- **refuse** - The opposite of match. Will stop and error on first match.

        "wait": 10,
        "retry": 2,
        "delay": 5,
        "verify": [ "MAPREDUCE:RCPT" ],
        "match": [
            [ "250", "Sender OK" ]
            [ "250", "Recipient OK" ]
            [ "MAPREDUCE:RCPT", "Custody=true", "Storage=check" ]
            [ "250", "Received OK" ]
        ],
        "refuse": [
            [ "java.lang.NullPointerException" ]
        ]


Case
----

    {
        "mx": [
            "example.com"
        ],
        "port": 25,

        "tls": true,
        "protocols": [
            "TLSv1", "TLSv1.1", "TLSv1.2"
        ],

        "ehlo": "example.com",

        "auth": true,
        "user": "tony@example.com",
        "pass": "giveHerTheRing",

        "mail": "tony@example.com",
        "rcpt": [
            "pepper@example.com",
            "happy@example.com"
        ],

        "envelopes": [
            {
                "chunkSize": 20480,
                "chunkBdat": true,
                "chunkWrite": true,

                "file": "src/test/resources/lipsum.eml",

                "assertions": {
                    "smtp": [
                        [ "MAIL", "^250" ],
                        [ "RCPT", "^250" ],
                        [ "BDAT", "^250" ]
                    ],
                    "mta": {
                        "wait": 10,
                        "retry": 2,
                        "delay": 5,
                        "verify": [ "MAPREDUCE:RCPT" ],
                        "match": [
                            [ "250", "Sender OK" ]
                            [ "250", "Recipient OK" ]
                            [ "MAPREDUCE:RCPT", "Custody=true", "Storage=check" ]
                            [ "250", "Received OK" ]
                        ]
                    }
                }
            }
        ],

        "assertions": {
            "smtp": [
                [ "SMTP", "^220" ],
                [ "EHLO", "STARTTLS" ],
                [ "SHLO", "250 HELP" ]
            ]
        }
    }


Configuration
-------------
*client.json*

    {
        "mx": [
            "example.com"
        ],
        "port": 25,
        
        "tls": true,
        "protocols": [
            "TLSv1.1", "TLSv1.2"
        ],
        "ciphers": [
            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384"
        ],
        
        "ehlo": "example.com",
        "mail": "tony@example.com",
        "rcpt": [
            "pepper@example.com",
            "happy@example.com"
        ],
        
        "routes": [
            {
                "name": "com",
                "mx": [
                    "example.com"
                ],
                "port": 25
            },
            
            {
                "name": "net",
                "mx": [
                    "example.net"
                ],
                "port": 465,
                "auth": true,
                "user": "tony@example.com",
                "pass": "giveHerTheRing"
            }
        ]
    }

 Maven execution
----------------
Example commandline for running cases.

    mvn clean -D test="cases.*Case" test

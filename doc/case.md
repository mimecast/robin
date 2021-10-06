Test Cases
==========
A framework for writing tests for automation.


Glossary
--------

### Session
#### Connection
- **retry** - _(Integer, Attempts)_ [default: 1] How many times to attempt connection.
- **delay** - _(Integer, Seconds)_ [default: 0] Delay between connection retries.
- **timeout** - _(Integer, Milliseconds)_ [default: Java] Socket timeout.


#### Destination
- **mx** - _(List, String, IP, FQD)_ [default: 127.0.0.1] MX server list.
- **port** - _(Integer)_ [default: 25] MX port.
- **route** - _(String, Name)_ [default: none] Selects a preconfigured route by name from _client.json_ routes if any.


#### Encryption
- **tls** - _(Boolean)_ [default: false] Allows the client to do TLS when _STARTTLS_ is advertised.
- **protocols** - _(List, String)_ [default: Java] TLS protocols to support.
- **ciphers** - _(List, String)_ [default: Java] TLS ciphers to support.


#### Authentication
- **auth** - _(Boolean)_ [default: false] SMTP authentication.
- **user** - _(String)_ Authentication username.
- **pass** - _(String)_ Authentication password.


#### Authentication Config
- **authBeforeTls**  - _(Boolean)_ [default: false] Send _AUTH_ withot requireing TLS. **_Vulnerable behaviour._**
- **authLoginCombined**  - _(Boolean)_ [default: false] Send username and password in one line for _AUTH LOGIN_.
- **authLoginRetry**  - _(Boolean)_ [default: false] Disable authLoginCombined and retry _AUTH LOGIN_.


#### EHLO
- **ehlo** - _(String, IP, FQD)_ [default: hostname] _EHLO_ domain.


#### XCLIENT
- **xclient** - _(SMTP command)_ Special feature allowing enumation of client info:
  - addr - IP address.
  - name - Reverse DNS.
  - helo - HELO/EHLO domain.


### Envelope
- **mail** - _(String, Email Address)_ [default: client.json] Sender email address.
- **rcpt** - _(List, String, Email Address)_ [default: client.json] Recipients list of email addresses.
- **mailEjf** - _(String)_ Special variable for EJF magic. Value _{$mail}_ will use _client.json_ mail value.
- **rcptEjf** - _(String)_ Special variable for EJF magic. Value _{$rcpt}_ will use _client.json_ rcpt value.


#### Transfer
##### BDAT Config
- **chunkSize** - _(Integer, Bytes)_ [default: 2048, min: 128] Enables _CHUNKING_ if grater than minimum.
- **chunkBdat**  - _(Boolean)_ [default: false] Writes _BDAT_ command to socket along with the first chunk.
- **chunkWrite** - _(Boolean)_ [default: false] Writes to socket in uneven chunks between 1024 and 2048 bytes if _chunkSize_ at least 2048.


##### DATA Config
_Only one may be used at the same time (Order of priority)._
- **terminateAfterBytes** - _(Integer, Bytes)_ [default: 0, min: 1] Terminates connection after transfering given bytes of _DATA_ when greater. Enables _terminateBeforeDot_.
- **terminateBeforeDot** - _(Boolean)_ [default: false] Terminates connection right before transfering _DATA_ terminator &lt;CRLF&gt;.&lt;CRLF&gt;.
- **terminateAfterDot** - _(Boolean)_ [default: false] Terminates connection right after transfering _DATA_ terminator &lt;CRLF&gt;.&lt;CRLF&gt;.


##### Speed
- **slowBytes** - _(Integer, Bytes)_ [default: 1, min: 128]  Adds a write delay every given number of bytes.
- **slowWait** - _(Integer, Milliseconds)_ [default: 0, min: 100]  Wait time in milliseconds.


##### Load
- **repeat** - _(Integer, Times)_ [default: 0]  How many times to ressed the same envelope after the first time. Will stop if any one delivery fails.


#### Message (envelope)
Every message should have either a file or a subject/message pair!
If a file is not defined a MIME source will be generated from envelope date along with subject and message.
Shile message is mandatory subject may be left blank.

- **file** - _(String, Path)_ Path to eml file to be transmitted.
- **folder** - _(String, Path)_ Path to eml containing folder. A random eml file will be chosen for each DATA transmission.
- **subject** - _(String)_ Email subject.
- **message** - _(String)_ Email text/plain message.


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

- **wait** - _(Integer, Seconds)_ [default: 2, min: 2] Initial wait before calling the logs client.
- **retry** - _(Integer, Attempts)_ [default: 1, min: 1] How many times to attempt to fetch logs.
- **delay** - _(Integer, Attempts)_ [default: 2, min: 2] Delay between attempts.
- **verify** - _(List, String, Regex)_ List of regex matches to verify bottom most needed logs received. Provides stability when MTA takes more time.
- **match** - _(List of List, String, Regex)_ Regex assertions to run against log lines. Multiple expressions can run on the same line. All must match.
- **refuse** - _(List of List, String, Regex)_ The opposite of match. Will stop and error on first match.

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

                "terminateAfterBytes": 1024,
                "terminateBeforeDot": true,
                "terminateAfterDot": true,

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

    mvn clean -D test="cases.*" test

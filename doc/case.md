E/SMTP Cases
============
Cases represent speciffic scenarios that can be configured in JSON and executed as a test for end-to-end testing of MTA servers.


Glossary
--------

### Session
#### Connection
- `retry` - _(Integer, Attempts)_ [default: 1] How many times to attempt connection.
- `delay` - _(Integer, Seconds)_ [default: 0] Delay between connection retries.
- `timeout` - _(Integer, Milliseconds)_ [default: Java] Socket timeout.


#### Destination
- `mx` - _(List, String, IP, FQD)_ [default: 127.0.0.1] MX server list.
- `port` - _(Integer)_ [default: 25] MX port.
- `route` - _(String, Name)_ [default: none] Selects a preconfigured route by name from _client.json5_ routes if any.


#### Encryption
- `tls` - _(Boolean)_ [default: false] Allows the client to do TLS when _STARTTLS_ is advertised.
- `protocols` - _(List, String)_ [default: Java] TLS protocols to support.
- `ciphers` - _(List, String)_ [default: Java] TLS ciphers to support.


#### Authentication
- `auth` - _(Boolean)_ [default: false] SMTP authentication.
- `user` - _(String)_ Authentication username.
- `pass` - _(String)_ Authentication password.


#### Authentication Config
- `authBeforeTls`  - _(Boolean)_ [default: false] Send _AUTH_ withot requireing TLS. **_Vulnerable behaviour._**
- `authLoginCombined`  - _(Boolean)_ [default: false] Send username and password in one line for _AUTH LOGIN_.
- `authLoginRetry`  - _(Boolean)_ [default: false] Disable authLoginCombined and retry _AUTH LOGIN_.


#### EHLO
- `ehlo` - _(String, IP, FQD)_ [default: hostname] _EHLO_ domain.


#### XCLIENT
- `xclient` - _(SMTP command)_ Special feature allowing enumation of client info:
  - addr - IP address.
  - name - Reverse DNS.
  - helo - HELO/EHLO domain.


### Envelope
- `mail` - _(String, Email Address)_ [default: client.json5] Sender email address.
- `rcpt` - _(List, String, Email Address)_ [default: client.json5] Recipients list of email addresses.

- `params` - Allows usage of custom parameters for special enviroments.

Will auto-inject list elements at the end of the MAIL or RCPT commands separated by space.

**Example case config:**

        params: {
            MAIL: [ "XOORG=example.com" ],
            RCPT: [ "ACCEPT" ]
        },

- `headers` - _(List, String, String)_ List of headers to be injected by magic.

Handy for EJF automation and more.

**Example case config:**

This uses magic client variables. See [magic.md](magic.md).

        headers: {
            from: "{$mail}",
            to: [ "{$rcpt}" ],
            sender: "{$mail}",
            recipient: [ "{$rcpt}" ]
        },

Can be injected in the eml via `{$HEADERS}` magic variable or selective by providing the key like `{$HEADERS[FROM]}`.


#### Transfer
##### BDAT Config
- `chunkSize` - _(Integer, Bytes)_ [default: 2048, min: 128] Enables _CHUNKING_ if grater than minimum.
- `chunkBdat`  - _(Boolean)_ [default: false] Writes _BDAT_ command to socket along with the first chunk.
- `chunkWrite` - _(Boolean)_ [default: false] Writes to socket in uneven chunks between 1024 and 2048 bytes if _chunkSize_ at least 2048.


##### DATA Config
_Only one may be used at the same time (Order of priority)._
- `terminateAfterBytes` - _(Integer, Bytes)_ [default: 0, min: 1] Terminates connection after transfering given bytes of _DATA_ when greater. Enables _terminateBeforeDot_.
- `terminateBeforeDot` - _(Boolean)_ [default: false] Terminates connection right before transfering _DATA_ terminator &lt;CRLF&gt;.&lt;CRLF&gt;.
- `terminateAfterDot` - _(Boolean)_ [default: false] Terminates connection right after transfering _DATA_ terminator &lt;CRLF&gt;.&lt;CRLF&gt;.


##### Speed
- `slowBytes` - _(Integer, Bytes)_ [default: 1, min: 128]  Write delay every n number of bytes.
- `slowWait` - _(Integer, Milliseconds)_ [default: 0, min: 100]  Write delay wait time in milliseconds.


##### Load
- `repeat` - _(Integer, Times)_ [default: 0]  How many times to resend the same envelope after the first time. Will stop if any delivery fails.


#### Message (envelope)
Every message should have either a file or a subject/message pair!
If a file is not defined a MIME source will be generated from envelope date along with subject and message.
Shile message is mandatory subject may be left blank.

- `file` - _(String, Path)_ Path to eml file to be transmitted.
- `folder` - _(String, Path)_ Path to eml containing folder. A random eml file will be chosen for each DATA transmission.
- `subject` - _(String)_ Email subject.
- `message` - _(String)_ Email text/plain message.


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
A local logs client is provided to deemonstrate.

_See LogsClient.java interface for implementation of external clients._

- `logPrecedence` - _(String)_ Prepends provided string to the log filename which is otherwise `yyyyMMdd`.log.
- `wait` - _(Integer, Seconds)_ [default: 2, min: 2] Initial wait before calling the external client.
- `retry` - _(Integer, Attempts)_ [default: 1, min: 1] How many times to attempt if verify fails.
- `delay` - _(Integer, Attempts)_ [default: 2, min: 2] Delay between attempts.
- `verify` - _(List, String, Regex)_ List of regex matches to verify bottom most needed logs received. Provides stability when MTA takes more time.
- `match` - _(List of List, String, Regex)_ Regex assertions to run against log lines. Multiple expressions can run on the same line. All must match.
- `refuse` - _(List of List, String, Regex)_ The opposite of match. Will stop and error on first match.

        type: "logs",
        logPrecedence: "fast-",
        wait: 10,
        retry: 2,
        delay: 5,
        verify: [ "MAPREDUCE:RCPT" ],
        match: [
            [ "250", "Sender OK" ]
            [ "250", "Recipient OK" ]
            [ "MAPREDUCE:RCPT", "Custody=true", "Storage=check" ]
            [ "250", "Received OK" ]
        ],
        refuse: [
            [ "java.lang.NullPointerException" ]
        ]

#### Humio

With the addition of Humio plugin you can simply use `type: "humio",` instead of logs to pull logs from configured Humio instance.

##### Humio config in properties.json

        humio: {
            auth: "YOUR_API_KEY",
            url: "https://humio.example.com/"
        }


Case
----

    {
        // Magic variables.
        $: { // Use the dollar sign to define a map to hold them.
            fromAddress: "robin@mimecast.net",
            toUser: "smoke",
            toDomain: "mta11.goldcheesyfish.com",
            toAddress: "{$toUser}@{$toDomain}" // Can be used immediatelly after defined above.
        },
        
        // MX list and port to attempt to deliver the email to.
        mx: [
            "example.com"
        ],
        port: 25,

        // How many times to try and establish a connection to remote server. First counts.
        retry: 2,

        // Delay between tries.
        delay: 5,
    
        // Enable TLS.
        tls: true,

        // Set supported protocols.
        protocols: [
            "TLSv1.2", "TLSv1.3"
        ],

        // Set EHLO to use.
        ehlo: "example.com",

        // Enable authentication.
        auth: true,
        user: "tony@example.com",
        pass: "giveHerTheRing",


      // Email envelopes.
      envelopes: [
        // Envelope one.
        {
                // Configure chunking parameters to use if CHUNKING supported by recipient server.
                chunkSize: 20480,
                chunkBdat: true,
                chunkWrite: true,

                // Magic sender and recipient.
                mail: "{$fromAddress}",
                rcpt: [
                    "{$toAddress}"
                ],

                // Set custom params to send with MAIL and/or RCPT.
                params: {
                    MAIL: [ "XOORG=example.com" ],
                    RCPT: [ "ACCEPT" ]
                },

                // Email eml file to transmit.
                file: "src/test/resources/cases/sources/lipsum.eml",
      
                // Configure early email transfer termination.
                terminateAfterBytes: 1024,
                terminateBeforeDot: true,
                terminateAfterDot: true,

                // Assertions to run against the envelope.
                assertions: {
          
                    // Protocol assertions.
                    // Check SMTP responses match regular expressions.
                    smtp: [
                        [ "MAIL", "^250" ],
                        [ "RCPT", "^250" ],
                        [ "BDAT", "^250" ]
                    ],
    
                    // External assertions.
                    external: [
                      {
                        // Assertion type logs for pulling logs from FFS.
                        type: "logs",
            
                        // How long to wait before asserting.
                        wait: 10,
            
                        // How many times to retry should no logs be found or valid.
                        retry: 2,
            
                        // Delay between log pulling attempts.
                        delay: 5,
            
                        // Verify regular expressions that would confirm logs fetched are complete.
                        verify: [ "MAPREDUCE:RCPT" ],
            
                        // Match regular expressions to run agaisnt logs to pass the test.
                        match: [
                            [ "250", "Sender OK" ]
                            [ "250", "Recipient OK" ]
                            [ "MAPREDUCE:RCPT", "Custody=true", "Storage=check" ]
                            [ "250", "Received OK" ]
                        ]

                        // Magic regular expressions that will record data in Session magic for use in following assertions.
                        // This will record group 1 is there is one else full match.
                        magic: [
                          {
                            name: "ruid",
                            pattern: "Received OK \\[(.*)\\]"
                          }
                        ]
                      }
                    ]
                }
            }
        ],

        // Assertions to run against the connection.
        assertions: {
            // Asserting configuration.
            smtpFails: false, // If SMTP assertion fails, fail test/exit gracefully.
            verifyFails: false, // If external verify checks fail, fail test/exit gracefully.
        
            // Protocol assertions.
            // Check SMTP responses match regular expressions.
            smtp: [
                [ "SMTP", "^220" ],
                [ "EHLO", "STARTTLS" ],
                [ "SHLO", "250 HELP" ]
            ]
        }
    }


Maven execution
----------------
Example commandline for running cases.

    mvn clean -D test="cases.*" test

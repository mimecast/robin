Server
======
Rudimentary debug server.
It supports user authentication and EHLO scenarios.
Outside of the given scenarios will accept everything.


Glossary
--------
- **bind** - Interface the server will bind too (default: ::).
- **port** - Port the server will listen too (default: 25).
- **backlog** - Number of connections to be allowed in the backlog (default: 25).
- **errorLimit** - Number of SMTP errors to allow before terminating connection (default: 3).
- **auth** - Advertise AUTH support (default: true).
- **starttls** - Advertise STARTTLS support (default: true).
- **chunking** - Advertise CHUNKING support (default: true).
- **keystore** - Java keystore (default: /usr/local/keystore.jks).
- **keystorepassword** - Keystore password (default: changeThis).
- **users** - Users allowed to authorize to the server.
- **scenarios** - Predefined server response scenarios based on EHLO value.


Configuration
-------------
*server.json*

    {
        "bind": "::",
        "port": 25,
        "backlog": 25,
        "errorLimit": 3,

        "auth": true,
        "starttls": true,
        "chunking": true,

        "keystore": "/usr/local/keystore.jks",
        "keystorepassword": "avengers",

        "users": [
            {
                "name": "tony@example.com",
                "pass": "giveHerTheRing"
            }
        ],

        "scenarios": {
            "ehlo.reject.com": {
                "ehlo": "501 Argument not allowed."
            },
            "mail.reject.com": {
              "mail": "451 Unable to process email at this time"
            },
            "rcpt.reject.com": {
                "rcpt": "501 Invalid address"
            },
            "data.reject.com": {
                "data": "554 Email rejected due to security policies"
            }
        }
    }

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
            "*": {
                "rcpt": [
                    {
                        "value": "friday\\-[0-9]+@example\\.com",
                        "response": "252 I think I know this user"
                    }
                ]
            },

            "reject.com": {
                "ehlo": "501 Not talking to you",
                "mail": "451 I'm not listening to you",
                "rcpt": [
                    {
                        "value": "ultron@reject\\.com",
                        "response": "501 Heart not found"
                    }
                ],
                "data": "554 Your data is corrupted"
            },

            "rejectmail.com": {
                "rcpt": [
                    {
                        "value": "jane@example\\.com",
                        "response": "501 Invalid address"
                    }
                ]
            },

            "helo.com": {
                "ehlo": "500 ESMTP Error (Try again using SMTP)"
            }
        }
    }

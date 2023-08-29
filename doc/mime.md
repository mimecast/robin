MIME
====
Using eml files is a cool way to guarantee a perfect reproduction when debugging but for testing purposes it is rarely needed.
In many cases you may simply want to just attach a file or add a piece of text.

With the MIME object you can construct basic emails on the fly as shown below:

    {
      $schema: "/schema/case.schema.json",

      retry: 2,

      envelopes: [
        {

Instead of defining a subject & message pair for a text/plain email, or a file for a MIME one,
You can now provide a list of headers and parts to have an email built at run time.

          mime: {


The email headers you wish to define.
Please note:
 - The `MIME-Version` headers is added first by default.
 - The following headers will be automatically created at run time if undefined:
 - - Date
 - - Message-ID
 - - Subject
 - - From
 - - To

            headers: [
              ["Subject", "Robin wrote"],
              ["To", "Sir Robin <sir@example.com>"],
              ["From", "Lady Robin <lady@example.com>"],
              ["X-Robin-Filename", "the.robins.eml"]
            ],

The parts also are optional, and you may add one if you need one.
If all you need is headers, ignore the parts.

            parts: [
              {

The parts may be either strings of paths.
In order to define a string use the `message` keyword and for a path `file`.

When adding a PDF attachment, you can either specify a pre-created file:

              {
                headers: [
                  ["Content-Type", "application/pdf; name=\"article.pdf\""],
                  ["Content-Disposition", "attachment; filename=\"article.pdf\""],
                  ["Content-Transfer-Encoding", "base64"]
                ],
                file: "src/test/resources/mime/robin.article.pdf"
              }

or dynamically generate a file using the magic variables:

              {
                headers: [
                  ["Content-Type", "application/pdf; name=\"article.pdf\""],
                  ["Content-Disposition", "attachment; filename=\"article.pdf\""],
                  ["Content-Transfer-Encoding", "base64"]
                ],
                pdf: {
                    text: "<p>{$RANDCH50} {$RANDNO10000}</p>\r\n<p>Robin had a party on {$DATE} it turned out to be a blast!</p>\r\n<p>{$RANDCH50} {$RANDNO10000}</p><hr/>",
                    image: "src/test/resources/mime/selfie.jpg"
               }
              }

JSON doesn't do multiline strings so best to stick with JSON5 to avoid long lines like in above example. 

The below example uses JSON5 multiline string. Don;t fetget the backslash. 

                headers: [
                  ["Content-Type", "text/plain; charset=\"UTF-8\""],
                  ["Content-Transfer-Encoding", "quoted-printable"]
                ],
            message: "Mon chéri,\
     \
     Please review this lovely blog post I have written about myself.\
     Huge ego, right?\
     \
     Kisses,\
     Your Robin."
              },
              {
                headers: [
                  ["Content-Type", "application/pdf; name=\"article.pdf\""],
                  ["Content-Disposition", "attachment; filename=\"article.pdf\""],
                  ["Content-Transfer-Encoding", "base64"]
                ],
                file: "src/test/resources/mime/robin.article.pdf"
              }
            ]
          },

          assertions: {
            protocol: [
              ["MAIL", "250 Sender OK"],
              ["RCPT", "250 Recipient OK"],
              ["DATA", "^250"],
              ["DATA", "Received OK"]
            ]
          }
        }
      ],

      assertions: {
        protocol: [
          [ "SMTP", "^220" ],
          [ "EHLO", "STARTTLS" ],
          [ "SHLO", "250 HELP" ],
          [ "QUIT" ]
        ]
      }
    }
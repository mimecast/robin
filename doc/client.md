Client
======
Highly customizable SMTP client.


Usage
-----
*For detailed explaination of each setting please refference case.md.*

    // Run once.
    AnnotationLoader.load(); // Load XCLIENT plugin and others if any.


    // Session.
    Session session = new Session() // Use XclientSession for XCLIENT capabilities.

            // Connection attempts.
            .setRetry(3)
            .setDelay(5)

            // Connection details.
            .setMx(Collections.singletonList("example.com"))
            .setPort(25)
            .setTimeout(60000)

            // TLS configuration.
            .setTls(true)
            .setAuthBeforeTls(false) // Do AUTH before STRTTLS.
            .setAuthLoginCombined(true) // Send username and password in one line for AUTH LOGIN.
            .setAuthLoginRetry(true) // Disable authLoginCombined and retry AUTH LOGIN.
            .setProtocols(new String[] { "TLSv1.2" })
            .setCiphers(new String[] { "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384", })

            // Hello domain.
            .setEhlo("example.com")

            // Authentication etails.
            .setAuth(true)
            .setUsername("tony@example.com")
            .setPassword("giveHerTheRing");


    // Envelope.
    MessageEnvelope envelope = new MessageEnvelope();

    // Parties.
    envelope.setMail("tony@example.net");
    envelope.getRcpts().add("pepper@example.com");
    envelope.getRcpts().add("happy@example.com");

    // Magic headers.
    envelope.addHeader("From", "jarvis@example.com");
    envelope.addHeader("To", "friday@example.com");


    // Email stream // Preferred when available.
    envelope.setStream(new FileInputStream(new File("src/test/resources/lipsum.eml")));

    // Email file // Preferred over subject and message.
    envelope.setFile("/Users/john/Documents/lost.eml");

    // Email subject and message // If stream and file undefined.
    envelope.setSubject("Lost in space");
    envelope.setMessage("Rescue me!");


    // Chunking // Options to emulate various ESP client behaviours.
    envelope.setChunkSize(10240); // Max bytes per BDAT chunk.
    envelope.setChunkBdat(true);  // Send BDAT command with the first part of the chunk in one TCP write.
    envelope.setChunkWrite(true); // Send chunk in uneven TCP writes between 1024 and 2048 bytes.


    // Add envelope to session.
    session.addEnvelope(envelope);


    // Send.
    new EmailDelivery(session).send();


/**
 * Application core.
 *
 * <h2>Client/ClientCLI</h2>
 * <br>SMTP delivery client.
 *
 * <p>Can be invoked via CLI or programatically.
 *
 * <p>While it can function like a standard email client, it was primarly designed for testing.
 * <br>Making it language agnostic allows QA enginners to rite tests easily without the need to know Java.
 *
 * <p>In this example we simply provide a config directory and a case JSON file:
 * <pre>
 *     &#64;Test
 *     void lipsum() throws AssertException, IOException, ConfigurationException {
 *         new Client("src/main/resources/")
 *                 .send("src/test/resources/cases/config/lipsum.json");
 *     }
 * </pre>
 *
 * <p>The foundation abstract ensures the configurations are loaded just once along with any plugins.
 *
 * <h2>Server/ServerCLI</h2>
 * <br>SMTP receipt server.
 *
 * <p>Can be invoked via CLI or programatically.
 * <br>It only requires a directory path for it's config files.
 *
 * <h2>Extensions</h2>
 * <br>SMTP extensions.
 *
 * <p>This being a SMTP client/server it operates based on SMTP extensions.
 *
 * <p>These are implemented in pairs of classes one for each side (client/server).
 *
 * <p>All standard extensions are provided, XCLIENT extension is offered via a plugin.
 *
 * <h2>Factories</h2>
 * <br>For all other pluggable components.
 * <br>Interfaces and default implementations are provide din most cases.
 * <ul>
 *     <li><b>Behaviour</b> - Dictates the behaviour of the SMTP client. <i>With new extensions come new behaviours.</i>
 *     <li><b>Session</b> - Holds the SMTP session data for both client and server. <i>With new extensions come new responsabilities.</i>
 *     <li><b>TLSSocket</b> - Negociates the TLS handshake for STARTTLS SMTP extension.
 *     <li><b>X509TrustManager</b> - Provides the means to validate remote certificates.
 *     <li><b>DigestCache</b> - Designed to hold the authentication cache for Digest-MD5 authentication mechanism subsequent authentication support. <i>When a dev gets bored.</i>
 *     <li><b>StorageClient</b> - Provides the means to save incoming emails to the disk or remotely.
 *     <li><b>ExternalClient</b> - Provides the means to fetch logs from other services to assert against (like MTA logs). <i>No default provided.</i>
 * </ul>
 *
 * <h2>Config</h2>
 * <br>Static container for client, server and properties configuration files.
 * <ul>
 *     <li><b>Client</b> - Client configuration defaults (these can be overriden by a case config) and predefined routes.
 *     <li><b>Server</b> - Server configuration, including authentication users and rejection scenarios.
 *     <li><b>Properties</b> - Universal configuration that also accesses system properties.
 * </ul>
 */
package com.mimecast.robin.main;

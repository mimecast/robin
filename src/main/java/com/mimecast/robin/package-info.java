/**
 * About Robin!
 *
 * <p>Robin MTA Tester is a development, debug and testing tool for MTA architects.
 * <br>It is powered by a highly customizable SMTP client designed to emulate the behaviour of popular email clients.
 * <br>A rudimentary server is also provided that is mainly used for testing the client.
 *
 * <p>The primary usage is done via JSON files called test cases.
 * <br>Cases are client configuration files ran as Junit tests.
 *
 * <p>This project can be compiled into a runnable JAR.
 * <br>A CLI interface is implemented with support for both client and server execution.
 *
 * <p>CLI usage:
 * <pre>
 *      $ java -jar robin.jar
 *      MTA development, debug and testing tool
 *
 *      usage:
 *      --client   Run as client
 *      --server   Run as server
 * </pre>
 * <pre>
 *      $ java -jar robin.jar --client
 *      Email delivery client
 *
 *      usage:
 *      -c,--conf &lt;arg&gt;    Path to configuration dir (Default: cfg/)
 *      -f,--file &lt;arg&gt;    EML file to send
 *      -h,--help         Show usage help
 *      -j,--gson &lt;arg&gt;    Path to case file JSON
 *      -m,--mail &lt;arg&gt;    MAIL FROM address
 *      -p,--port &lt;arg&gt;    Port to connect to
 *      -r,--rcpt &lt;arg&gt;    RCPT TO address
 *      -x,--mx &lt;arg&gt;      Server to connect to
 * </pre>
 * <pre>
 *      $ java -jar robin.jar --server
 *      Debug MTA server
 *
 *      usage:
 *      Path to configuration directory
 *
 *      example:
 *      java -jar robin.jar --server config/
 * </pre>
 *
 * <p>Mimecast uses this to run smoke tests every time a new MTA snapshot is built.
 * <br>This helps identify bugs early before leaving the development environment.
 */
package com.mimecast.robin;

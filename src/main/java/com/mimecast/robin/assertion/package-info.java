/**
 * Assertion core.
 *
 * <p>Client deliveries end with a call to Assert class.
 * <br>If any assertions are configured it will run them.
 *
 * <p>The idea is to have a language agnostic way of configuring smoke tests that can be automated.
 * <br>Cases can be executed via Maven with:
 * <pre>
 *     mvn clean -D test="cases.*" test
 * </pre>
 *
 * <h2>Example configuration:</h2>
 * <pre>
 *     "assertions": {
 * </pre>
 *
 * <p>Simple SMTP assertions that check the responses got were expected.
 * <pre>
 *         "smtp": [
 *             ["MAIL", "250 Sender OK"],
 *             ["RCPT", "250 Recipient OK"],
 *             ["DATA", "^250"],
 *             ["DATA", "Received OK"]
 *         ],
 * </pre>
 *
 * <p><b>Session SMTP assertions:</b>
 * <ul>
 *     <li><b>SMTP</b> - Initial connection response.
 *     <li><b>EHLO</b>
 *     <li><b>STARTTLS</b>
 *     <li><b>TLS</b> - Successfull handshake protocol / cipher.
 *     <li><b>SHLO</b> - Post STARTLS EHLO response.
 *     <li><b>XCLIENT</b>
 *     <li><b>XHLO</b> - Post XCLIENT EHLO response.
 *     <li><b>AUTH</b>
 *     <li><b>RSET</b>
 *     <li><b>HELP</b>
 *     <li><b>QUIT</b>
 * </ul>
 *
 * <p><b>Envelope SMTP assertions:</b>
 * <ul>
 *     <li><b>MAIL</b>
 *     <li><b>RCPT</b>
 *     <li><b>DATA/BDAT</b> - Mutully exclusive.
 * </ul>
 *
 * <p>External assertions with configurable pulling.
 * <pre>
 *         "key": {
 *           "wait": 5,
 *           "delay": 5,
 *           "retry": 3,
 *           "verify": ["MTAEMAILEXPLODE"],
 *           "match": [
 *             ["MTAEMAILEXPLODE", "Skel=Aph-"]
 *           ],
 *           "refuse": [
 *             ["java.lang.NullPointerException"]
 *           ]
 *         }
 * </pre>
 *
 * <ul>
 *     <li><b>wait</b> - <i>(Integer, Seconds)</i> [default: 2, min: 2] Initial wait before calling the logs client.
 *     <li><b>delay</b> - <i>(Integer, Attempts)</i> [default: 2, min: 2] Delay between attempts.
 *     <li><b>retry</b> - <i>(Integer, Attempts)</i> [default: 1, min: 1] How many times to attempt to fetch logs.
 *     <li><b>verify</b> - <i>(List, String, Regex)</i> List of regex matches to verify bottom most needed logs received. Provides stability when MTA takes more time.
 *     <li><b>match</b>  - <i>(List of List, String, Regex)</i> Regex assertions to run against log lines. Multiple expressions can run on the same line. All must match.
 *     <li><b>refuse</b> - <i>(List of List, String, Regex)</i> The opposite of match. Will stop and error on first match.
 * </ul>
 *
 * <pre>
 *     }
 * </pre>
 */
package com.mimecast.robin.assertion;

Plugins
=======

A number of components can be replaced/extended by the use of a plugin annotation.
SMTP XCLIENT extension is provided in this fasion to demonstrate the use case.
To allow for specific loading order a priority can be provided (default 100).


Example
-------

    @Plugin(priority=101)
    public class XclientPlugin {
        public XclientPlugin() {
            Factories.setBehaviour(XclientBehaviour::new);
            Factories.setSession(XclientSession::new);
            Extensions.addExtension("xclient", new Extension(ServerXclient::new, ClientXclient::new));
        }
    }

Factories
---------
The following components may be added/replaced:

- **Behaviour** - Provides the behaviour logic for the SMTP client. *See: DefaultBehaviour.java*
    
        Factories.setBehaviour(Behaviour::new)

- **Session** - SMTP Session data container. *See: Session.java*

        Factories.setSession(Session::new)

- **TLSSocket** - TLS implementation. *See: DefaultTLSSocket.java*

        Factories.setTLSSocket(TLSSocket::new)

- **X509TrustManager** - TrustManager implementation. *See: PermissiveTrustManager.java*

        Factories.setTrustManager(X509TrustManager::new)

- **DigestDatabase** - Deque storage map. *See: StaticDigestDatabase.java*

        Factories.setDatabase(DigestDatabase::new)

- **LogsClient** - MTA logs client. *See: LogsClient.java*

        Factories.setLogsClient(LogsClient::new)



Extensions
----------
The following SMTP extensions exist by default.
Adding one by the same name will replace an existing one.

Both server and client callable should be provided but this is not enforced.
However adding a null client or server implementation will result in a NullPointerException at runtime.

- HELO
- EHLO
- STARTTLS
- AUTH
- MAIL
- RCPT
- DATA
- BDAT
- RSET
- HELP
- QUIT



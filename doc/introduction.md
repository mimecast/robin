Introduction
============
Before you start the server or run the example clients you should start by checking the config files
and make any required changes according to your environment.


Robin client
------------
Is designed to use minimal config and as such it loads defaults from [client.json5](../cfg/client.json5).
You may want to adjust the following values: mx, port, ehlo, main and rcpt.
Additionaly you may want to configure your own routes for even more streamlined case config.

- See ExampleSend.java for examples on how to craft JSON5 cases to send emails.
  - Read [case.md](case.md), [magic.md](magic.md) and [mime.md](mime.md). 
- See ExampleHttp.java for examples on how to craft JSON5 cases to do HTTP requests.
  - Read [request.md](request.md). 
- See ExampleProgramatic.java for examples on how to use it as a Java library.
    - Read [client.md](client.md) and to extend its capabilities [plugin.md](plugin.md).
- Read [cli.md](cli.md) for commandline usage.


Robin server
------------
It might need some configuring as well in  [server.json5](../cfg/server.json5) in order to be able to load a keystore. One is provided in test resources.
Additionaly you may want to update the email store path.

- Read [cli.md](cli.md) for commandline usage.


_Use it wisely!_

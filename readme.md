Robin MTA Tester
================
By **Vlad Marian** *<vmarian@mimecast.com>*


Overview
--------
<img align="right" width="200" height="200" src="doc/logo.jpg">
Robin MTA Tester is a development, debug and testing tool for MTA architects.
It is powered by a highly customizable SMTP client designed to emulate the behaviour of popular email clients.
A rudimentary server is also provided that is mainly used for testing the client.

The primary usage is done via JSON files called test cases.
Cases are client configuration files ran as Junit tests.

This project can be compiled into a runnable JAR.
A CLI interface is implemented with support for both client and server execution.

Mimecast uses this to run smoke tests every time a new MTA snapshot is built.
This helps identify bugs early before leaving the development environment.


Contributions
-------------
Contributions of any kind (bug fixes, new features...) are welcome!
This is a development tool and as such it may not be perfect and may be lacking in some areas.

Certain future functionalities are marked with TODO comments throughout the code.
This however does not mean they will be given priority or ever be done.

Any merge request made should align to existing coding style and naming convention.
Before submitting a merge request please run a comprehensive code quality analysis (IntelliJ, SonarQube).


How to:
-----------
- [Client usage](doc/client.md)
- [Server configuration](doc/server.md)
- [Test Cases](doc/case.md)
- [Magic variables](doc/magic.md)
- [Plugins](doc/plugin.md)
- [CLI usage](doc/cli.md)

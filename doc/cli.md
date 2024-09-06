Command line usage
==================

    java -jar robin.jar
     MTA development, debug and testing tool

    usage:
        --client   Run as client
        --server   Run as server

Client
------

    java -jar robin.jar --client
     Email delivery client

    usage:
     -c,--conf <arg>   Path to configuration dir (Default: cfg/)
     -f,--file <arg>   EML file to send
     -h,--help         Show usage help
     -j,--gson <arg>   Path to case file JSON
     -m,--mail <arg>   MAIL FROM address
     -p,--port <arg>   Port to connect to
     -r,--rcpt <arg>   RCPT TO address
     -x,--mx <arg>     Server to connect to

Server
------

    java -jar robin.jar --server
     Debug MTA server

    usage:
     Path to configuration directory

    example:
     java -jar robin.jar --server cfg/

Common
------

The Log4j2 XML filename can be configured via properties.json5 or a system property called `log4j2`.

    example:
     java -jar robin.jar --server cfg/ -Dlog4j2=log4j2custom.xml

The properties.json5 filename can be configured via a system property called `properties`.

    example:
     java -jar robin.jar --server cfg/ -Dproperties=properties-new.json5

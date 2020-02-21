package com.mimecast.robin.main;

import com.mimecast.robin.Main;

import javax.naming.ConfigurationException;

/**
 * Implementation of server CLI.
 *
 * @see Server
 */
public class ServerCLI {

    /**
     * Protected constructor.
     */
    private ServerCLI() {
        throw new IllegalStateException("Static class");
    }

    /**
     * Listener usage.
     */
    private static final String USAGE = Main.USAGE + " --server";

    /**
     * Listener description.
     */
    private static final String DESCRIPTION = "Debug MTA server";

    /**
     * Constructs a new ServerCLI instance.
     *
     * @param main Main instance.
     */
    public static void main(Main main) {
        if (main.getArgs().length > 0) {
            try {
                Server.run(main.getArgs()[0]);
            } catch (ConfigurationException e) {
                main.log("Server error: " + e.getMessage());
            }

        } else {
            main.log(USAGE);
            main.log(" " + DESCRIPTION);
            main.log("");
            main.log("usage:");
            main.log(" Path to configuration directory");
            main.log("");
            main.log("example:");
            main.log(" " + USAGE + " config/");
        }
    }
}

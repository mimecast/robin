package com.mimecast.robin.main;

import com.mimecast.robin.smtp.SmtpListener;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Socket listener.
 *
 * <p>This is the means by which the server is started.
 * <p>It's initilized with a configuration dir path.
 * <p>The configuration path is used to load the global configuration files.
 * <p>Loads both client and server configuration files.
 *
 * @see SmtpListener
 */
public class Server extends Foundation {

    /**
     * Listener instance.
     */
    private static SmtpListener port25;

    /**
     * Runner.
     *
     * @param path Directory path.
     * @throws ConfigurationException Unable to read/parse config file.
     */
    public static void run(String path) throws ConfigurationException {
        init(path); // Initialize foundation.
        registerShutdown(); // Shutdown hook.
        loadKeystore(); // Load Keystore.

        // Listener.
        port25 = new SmtpListener(
                Config.getServer().getPort(),
                Config.getServer().getBacklog(),
                Config.getServer().getBind()
        );
    }

    /**
     * Shutdown hook.
     */
    private static void registerShutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (port25 != null && port25.getListener() != null) {
                log.info("Service is shutting down.");
                try {
                    port25.serverShutdown();
                } catch (IOException e) {
                    log.info("Shutdown in progress.. please wait.");
                }
            }
        }));
    }

    /**
     * Load Keystore.
     */
    private static void loadKeystore() {
        // Check keystore file is readable.
        try {
            Files.readAllBytes(Paths.get(Config.getServer().getKeyStore()));
        } catch (IOException e) {
            log.error("Error reading keystore file: {}", e.getMessage());
        }
        System.setProperty("javax.net.ssl.keyStore", Config.getServer().getKeyStore());

        // Read keystore password from file.
        String keyStorePassword;
        try {
            keyStorePassword = new String(Files.readAllBytes(Paths.get(Config.getServer().getKeyStorePassword())));
        } catch (IOException e) {
            log.warn("Keystore password treated as text.");
            keyStorePassword = Config.getServer().getKeyStorePassword();
        }
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
    }
}

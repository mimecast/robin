package com.mimecast.robin.main;

import com.mimecast.robin.smtp.extension.Extension;
import com.mimecast.robin.smtp.extension.client.*;
import com.mimecast.robin.smtp.extension.server.*;
import com.mimecast.robin.smtp.verb.Verb;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * SMTP server and client supported verbs.
 * <p>Every SMTP verb is implemented by an extension.
 * <p>Each extension has a server and a client implementation.
 * <p>The server will select the appropriate extension to the SMTP verb it receives.
 * <p>The client however has a behaviour which defines what command will be issued when.
 *
 * @see Verb
 * @see ClientProcessor
 * @see ServerProcessor
 * @see Behaviour
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class Extensions {

    /**
     * Extensions container.
     */
    private static final Map<String, Extension> map = new HashMap<>();

    /*
      Default extensions.
     */
     static {
        map.put("helo", new Extension(ServerEhlo::new, ClientEhlo::new));
        map.put("ehlo", new Extension(ServerEhlo::new, ClientEhlo::new));

        map.put("starttls", new Extension(ServerStartTls::new, ClientStartTls::new));
        map.put("auth", new Extension(ServerAuth::new, ClientAuth::new));

        map.put("mail", new Extension(ServerMail::new, ClientMail::new));
        map.put("rcpt", new Extension(ServerRcpt::new, ClientRcpt::new));

        map.put("data", new Extension(ServerData::new, ClientData::new));
        map.put("bdat", new Extension(ServerData::new, ClientData::new));

        map.put("rset", new Extension(ServerRset::new, ClientRset::new));
        map.put("help", new Extension(ServerHelp::new, ClientHelp::new));
        map.put("quit", new Extension(ServerQuit::new, ClientQuit::new));
    }

    /**
     * Protected constructor.
     */
    private Extensions() {
        throw new IllegalStateException("Static class");
    }

    /**
     * Gets all extensions.
     *
     * @return Extensions map.
     */
    public static Map<String, Extension> getExtensions() {
        return map;
    }

    /**
     * Is extension supported by verb.
     *
     * @param verb Verb instance.
     * @return Boolean.
     */
    public static boolean isExtension(Verb verb) {
        if (verb == null) {
            throw new IllegalArgumentException("Verb cannot be null");
        }

        return isExtension(verb.getKey());
    }

    /**
     * Is extension supported by name.
     *
     * @param name Extension name.
     * @return Boolean.
     */
    public static boolean isExtension(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }

        return map.containsKey(name.toLowerCase());
    }

    /**
     * Gets a extension by verb.
     *
     * @param verb Verb instance.
     * @return Optional of Extension.
     */
    public static Optional<Extension> getExtension(Verb verb) {
        if (verb == null) {
            throw new IllegalArgumentException("Verb cannot be null");
        }

        return getExtension(verb.getKey());
    }

    /**
     * Gets a extension by name.
     *
     * @param name Extension name.
     * @return Optional of Extension.
     */
    public static Optional<Extension> getExtension(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        return Optional.ofNullable(map.get(name.toLowerCase()));
    }

    /**
     * Adds an extension.
     *
     * @param name Extension name.
     * @param pair Pair of server and client implementation callable.
     */
    public static void addExtension(String name, Extension pair) {
        if (name == null || pair == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }

        map.put(name.toLowerCase(), pair);
    }

    /**
     * Removed an extension.
     *
     * @param name Extension name.
     */
    public static void removeExtension(String name) {
        map.remove(name.toLowerCase());
    }

    /**
     * Gets help message from names of extensions.
     * <p>This is used to generate the HELP response.
     *
     * @return Help message.
     */
    public static String getHelp() {
        return String.join(" ", map.keySet().toArray(new String[0])).toUpperCase();
    }
}

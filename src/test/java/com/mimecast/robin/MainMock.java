package com.mimecast.robin;

import java.util.ArrayList;
import java.util.List;

/**
 * CLI runnable.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public final class MainMock extends Main {

    /**
     * Logs list.
     */
    private List<String> logs;

    /**
     * Main runnable.
     *
     * @param argv List of String.
     */
    public static List<String> main(List<String> argv) {
        MainMock main = new MainMock(argv.toArray(new String[0]));
        return main.getLogs();
    }

    /**
     * Constructs a new Main instance.
     *
     * @param args String array.
     */
    private MainMock(String[] args) {
        super(args);
    }

    /**
     * Logging wrapper.
     *
     * @param string String.
     */
    @Override
    public void log(String string) {
        super.log(string);
        if (logs == null) {
            logs = new ArrayList<>();
        }
        logs.add(string);
    }

    /**
     * Gets logs.
     *
     * @return List of String.
     */
    private List<String> getLogs() {
        return logs;
    }
}

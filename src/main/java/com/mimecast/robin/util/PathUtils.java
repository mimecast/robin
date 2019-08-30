package com.mimecast.robin.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Path handling static utilities.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public final class PathUtils extends File {

    /**
     * Creates a new PathUtils instance.
     *
     * @param  path A pathname string
     * @throws NullPointerException If the path argument is null.
     */
    private PathUtils(String path) {
        super(path);
    }

    /**
     * Check if file exists.
     *
     * @param path File path.
     */
    public static String validatePath(String path) throws IOException {
        return validatePath(path, path);
    }

    /**
     * Check if file exists with exception.
     *
     * @param path      File path.
     * @param exception Exception message to throw if it excepts.
     */
    public static String validatePath(String path, String exception) throws IOException {
        if (isFile(path)) return path;
        throw new IOException(exception);
    }

    /**
     * Check if file exists boolean.
     *
     * @param path File path.
     * @return Boolean.
     */
    public static boolean isFile(String path) {
        return path != null && new PathUtils(path).isFile();
    }

    /**
     * Check if directory exists boolean.
     *
     * @param path Directory path.
     * @return Boolean.
     */
    public static boolean isDirectory(String path) {
        return path != null && new PathUtils(path).isDirectory();
    }

    /**
     * Get file contents as a string using given charset.
     *
     * @param path    File path.
     * @param charset Charset string.
     * @return String.
     * @throws IOException Unable to read file.
     */
    public static String readFile(String path, Charset charset) throws IOException {
        return isFile(path) ? new String(Files.readAllBytes(Paths.get(path)), charset) : "";
    }
}

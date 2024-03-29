package com.mimecast.robin.util;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Static utilities for handling files and paths.
 */
@SuppressWarnings("squid:S1192")
public final class PathUtils extends File {

    /**
     * Creates a new PathUtils instance.
     *
     * @param path A pathname string
     * @throws NullPointerException If the path argument is null.
     */
    private PathUtils(String path) {
        super(path);
    }

    /**
     * Is file readable.
     *
     * @param path File path.
     * @return Boolean.
     */
    public static boolean isFile(String path) {
        return path != null && new PathUtils(path).isFile();
    }

    /**
     * Normalizez file/directory name.
     *
     * @param path File/directory name.
     * @return Normalized string.
     */
    public static String normalize(String path) {
        Objects.requireNonNull(path, "path must not be null");
        return FilenameUtils.normalize(path).replaceAll(Pattern.quote(File.separator), "");
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
     * Makes directory path if not exists.
     *
     * @param path Directory path.
     * @return Boolean.
     */
    public static boolean makePath(String path) {
        Objects.requireNonNull(path, "path must not be null");
        return new PathUtils(path).isDirectory() || new PathUtils(path).mkdirs();
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
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(charset, "charset must not be null");
        return new String(Files.readAllBytes(Paths.get(path)), charset);
    }

    /**
     * Get random file from folder with optional extension filter
     *
     * @param path       Path to folder.
     * @param extensions List of String.
     * @return Absolute path.
     */
    public static String folderFile(String path, List<String> extensions) {
        Objects.requireNonNull(path, "path must not be null");

        final List<String> filter = extensions != null ? extensions : new ArrayList<>();

        File[] files = new File(path).listFiles();
        if (files != null && files.length > 0) {
            List<String> filtered = Stream.of(files)
                    .filter(file -> !file.isDirectory() && filter.contains(FilenameUtils.getExtension(file.getName().toLowerCase())))
                    .map(File::getName)
                    .collect(Collectors.toList());

            return Paths.get(path, filtered.get(new SecureRandom().nextInt(filtered.size()))).toString();
        }

        return null;
    }
}

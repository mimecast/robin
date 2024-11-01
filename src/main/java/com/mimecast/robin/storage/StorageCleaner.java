package com.mimecast.robin.storage;

import com.mimecast.robin.config.BasicConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Storage Cleaner.
 * <p>Cleans storage folder files with exclude pattern.
 */
public class StorageCleaner {
    protected static final Logger log = LogManager.getLogger(StorageCleaner.class);

    /**
     * Clean.
     *
     * @param config BasicConfig instance.
     */
    @SuppressWarnings("unchecked")
    public static void clean(BasicConfig config) {
        try {
            if (config.getBooleanProperty("clean")) {
                String path = config.getStringProperty("path");

                List<Pattern> patterns = ((List<String>) config.getListProperty("patterns"))
                        .stream()
                        .map(s -> Pattern.compile(s, Pattern.CASE_INSENSITIVE))
                        .collect(Collectors.toList());

                cleanDirectory(new File(path), false, patterns);
            }

        } catch (Exception e) {
            log.fatal("Error reading storage config {}.", e.getMessage());
        }
    }

    /**
     * Clean directory.
     *
     * @param directory File instance.
     * @param remove    Boolean true to delete provided folder.
     * @param patterns  List of Pattern instances.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void cleanDirectory(File directory, boolean remove, List<Pattern> patterns) {
        try {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        cleanDirectory(file, true, patterns);
                    } else {
                        for (Pattern p : patterns) {
                            if (p.matcher(file.getName()).find()) {
                                if (file.delete()) {
                                    log.trace("Removed file: {}", file.getAbsolutePath());
                                }
                                break;
                            }
                        }
                        if (file.exists()) {
                            log.trace("Skipped file: {}", file.getAbsolutePath());
                        }
                    }
                }
            }

            files = directory.listFiles();
            if (remove && files != null && files.length == 0) {
                if (directory.delete()) {
                    log.debug("Removed folder: {}", directory.getAbsolutePath());
                }
            }

        } catch (Exception e) {
            log.fatal("Error deleting storage: {}", e.getMessage());
        }
    }
}

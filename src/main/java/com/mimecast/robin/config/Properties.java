package com.mimecast.robin.config;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Locale;

/**
 * General purpose configuration.
 *
 * <p>This provides access to a generic properties file.
 * <p>It will also read primitives from system properties with priority.
 */
public class Properties extends ConfigFoundation {

    /**
     * Constructs a new Properties instance.
     */
    public Properties() {
        super();
    }

    /**
     * Constructs a new Properties instance with given file path.
     *
     * @param path File path.
     * @throws IOException Unable to read file.
     */
    public Properties(String path) throws IOException {
        super(path);
    }

    /**
     * Check a property exists with system property check.
     *
     * @param name Property name.
     * @return Boolean.
     */
    @Override
    public boolean hasProperty(String name) {
        return super.hasProperty(name) || System.getProperty(name) != null;
    }

    /**
     * Gets String property with system property priority.
     *
     * @param name Property name.
     * @return String.
     */
    @Override
    public String getStringProperty(String name) {
        String sys = System.getProperty(name);
        return StringUtils.isNotBlank(sys) ? sys : getStringProperty(name, null);
    }

    /**
     * Gets Long property with system property priority.
     *
     * @param name Property name.
     * @return Long.
     */
    @Override
    public Long getLongProperty(String name) {
        String sys = System.getProperty(name);
        return StringUtils.isNotBlank(sys) ? Long.valueOf(sys) : super.getLongProperty(name);
    }

    /**
     * Gets Boolean property with system property priority.
     *
     * @param name Property name.
     * @return Boolean.
     */
    @Override
    public Boolean getBooleanProperty(String name) {
        String sys = System.getProperty(name);
        return StringUtils.isNotBlank(sys) ? Boolean.valueOf(sys) : super.getBooleanProperty(name);
    }
    
    /**
     * Gets Locale property if set or default.
     */
    public Locale getLocale() {
        return LocaleUtils.toLocale(getStringProperty("locale", Locale.getDefault().toString()));
    }
}

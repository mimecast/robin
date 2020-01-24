package com.mimecast.robin.annotation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Plugin annotation loader.
 * <p>This class scans the plugin package at runtime to identify classes annotated with the @Plugin interface.
 * <p>After ordering them by priority it will instantiate each one in that order.
 * <p>Priority collision is not handled thus their order will be random.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public abstract class AnnotationLoader {
    private static final Logger log = LogManager.getLogger(AnnotationLoader.class);

    /**
     * Protected constructor.
     */
    private AnnotationLoader() {
        throw new IllegalStateException("Static class");
    }

    /**
     * Scans and instantiates plugins found.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void load() {
        Reflections reflections = new Reflections("com.mimecast.robin.annotation.plugin");
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Plugin.class);

        Map<Integer, List<Class>> tree = new TreeMap<>();

        for (Class<?> clazz : annotated) {
            Annotation[] annotations = clazz.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof Plugin) {
                    tree.putIfAbsent(((Plugin) annotation).priority(), new ArrayList<>());
                    // Because tree map put is broken and returns null.
                    tree.get(((Plugin) annotation).priority()).add(clazz);
                }
            }
        }

        for (Map.Entry<Integer, List<Class>> entry : tree.entrySet()) {
            for (Class clazz : entry.getValue()) {
                try {
                    log.debug("Plugin: {} priority={}", clazz.getName(), entry.getKey());
                    clazz.getDeclaredConstructor().newInstance();
                } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                    log.error("Error constructing instance for class: {}", e.getMessage());
                }
            }
        }
    }
}

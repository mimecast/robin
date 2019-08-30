package com.mimecast.robin.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * Plugin interface.
 * <p>Classes using this annotation should be placed inside the plugin package or will not be loaded.
 *
 * @see AnnotationLoader
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Plugin {

    /**
     * Execution priority.
     *
     * @return Annotation priority if given or default 100.
     */
    int priority() default 100;
}

package com.mimecast.robin.annotation;

import java.lang.annotation.*;

/**
 * Plugin annotation interface.
 *
 * <p>Classes using this annotation should be placed inside the plugin package or will not be loaded.
 *
 * @see AnnotationLoader
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

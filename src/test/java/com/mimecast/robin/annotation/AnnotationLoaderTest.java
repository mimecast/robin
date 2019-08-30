package com.mimecast.robin.annotation;

import com.mimecast.robin.main.Extensions;
import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotationLoaderTest {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("src/test/resources/");
    }

    @Test
    void load() {
        AnnotationLoader.load();
        assertTrue(Extensions.isExtension("xclient"));
    }
}

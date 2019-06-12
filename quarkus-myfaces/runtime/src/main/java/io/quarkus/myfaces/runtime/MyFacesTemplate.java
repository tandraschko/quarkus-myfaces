package io.quarkus.myfaces.runtime;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import io.quarkus.runtime.annotations.Template;

@Template
public class MyFacesTemplate {

    public static final Map<Class<? extends Annotation>, Set<Class<?>>> ANNOTATED_CLASSES = new LinkedHashMap<>();

    @SuppressWarnings("unchecked") //cast to (Class<? extends Annotation>)
    public void registerAnnotatedClass(String annotationName, String clazzName) {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            Class<? extends Annotation> annotation = (Class<? extends Annotation>) cl.loadClass(annotationName);
            Class<?> clazz = cl.loadClass(clazzName);

            Set<Class<?>> classes = ANNOTATED_CLASSES.computeIfAbsent(annotation, $ -> new HashSet<>());
            classes.add(clazz);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}

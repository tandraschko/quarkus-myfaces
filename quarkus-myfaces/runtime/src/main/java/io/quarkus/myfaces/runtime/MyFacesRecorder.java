package io.quarkus.myfaces.runtime;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.faces.model.DataModel;

import org.apache.myfaces.flow.FlowReference;
import org.apache.myfaces.util.lang.ClassUtils;

import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class MyFacesRecorder {

    public static final Map<Class<? extends Annotation>, Set<Class<?>>> ANNOTATED_CLASSES = new LinkedHashMap<>();
    public static final Map<Class<? extends DataModel>, Class<?>> FACES_DATA_MODELS = new LinkedHashMap<>();
    public static final Map<Class, FlowReference> FLOW_REFERENCES = new ConcurrentHashMap<Class, FlowReference>();

    @SuppressWarnings("unchecked") //cast to (Class<? extends Annotation>)
    public void registerAnnotatedClass(String annotationName, String clazzName) {
        Class<? extends Annotation> annotation = (Class<? extends Annotation>) ClassUtils.simpleClassForName(annotationName);
        Class<?> clazz = ClassUtils.simpleClassForName(clazzName);

        Set<Class<?>> classes = ANNOTATED_CLASSES.computeIfAbsent(annotation, $ -> new HashSet<>());
        classes.add(clazz);
    }

    @SuppressWarnings("unchecked") //cast to (Class<? extends DataModel>)
    public void registerFacesDataModel(String clazzName, String forClassName) {
        Class<? extends DataModel> clazz = ClassUtils.simpleClassForName(clazzName);
        Class<?> forClass = ClassUtils.simpleClassForName(forClassName);

        FACES_DATA_MODELS.put(clazz, forClass);
    }

    public void registerFlowReference(String clazzName, String definingDocumentId, String flowId) {
        Class<?> clazz = ClassUtils.simpleClassForName(clazzName);

        FLOW_REFERENCES.put(clazz, new FlowReference(definingDocumentId, flowId));
    }
}

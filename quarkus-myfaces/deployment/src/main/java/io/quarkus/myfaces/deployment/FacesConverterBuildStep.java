/*
 * Copyright 2019 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkus.myfaces.deployment;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import io.quarkus.arc.deployment.BeanRegistrarBuildItem;
import io.quarkus.arc.processor.BeanRegistrar;
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.myfaces.runtime.SimpleBeanCreator;

public class FacesConverterBuildStep {

    public static void build(BuildProducer<BeanRegistrarBuildItem> beanConfigurators,
            CombinedIndexBuildItem combinedIndex) {

        for (AnnotationInstance ai : combinedIndex.getIndex()
                .getAnnotations(DotName.createSimple(FacesConverter.class.getName()))) {

            AnnotationValue managed = ai.value("managed");
            if (managed != null && managed.asBoolean()) {
                AnnotationValue forClass = ai.value("forClass");
                if (forClass != null) {
                    if (!Object.class.getName().equals(forClass.asClass().name().toString())) {
                        register(beanConfigurators,
                                ai.target().asClass(), forClass.asClass(), null);
                    }
                }

                AnnotationValue value = ai.value("value");
                if (value != null) {
                    if (value.asString() != null && value.asString().length() > 0) {
                        register(beanConfigurators,
                                ai.target().asClass(), null, value.asString());
                    }
                }
            }
        }
    }

    private static void register(BuildProducer<BeanRegistrarBuildItem> beanConfigurators,
            ClassInfo clazz,
            Type forClass,
            String converterId) {

        if (converterId == null) {
            converterId = "";
        }
        if (forClass == null) {
            forClass = Type.create(DotName.createSimple(Object.class.getName()), Type.Kind.CLASS);
        }

        List<AnnotationValue> qualifierAttributes = Arrays.asList(
                AnnotationValue.createClassValue("forClass", forClass),
                AnnotationValue.createStringValue("value", converterId),
                AnnotationValue.createBooleanValue("managed", true));

        AnnotationInstance qualifier = AnnotationInstance.create(
                DotName.createSimple(FacesConverter.class.getName()),
                null,
                qualifierAttributes);

        beanConfigurators.produce(new BeanRegistrarBuildItem(new BeanRegistrar() {
            @Override
            public void register(BeanRegistrar.RegistrationContext registrationContext) {
                registrationContext
                        .configure(clazz.name())
                        .qualifiers(qualifier)
                        .scope(BuiltinScope.DEPENDENT.getInfo())
                        .types(Type.create(DotName.createSimple(Converter.class.getName()), Type.Kind.CLASS),
                                Type.create(clazz.name(), Type.Kind.CLASS))
                        .creator(SimpleBeanCreator.class)
                        .name(UUID.randomUUID().toString().replace("-", ""))
                        .defaultBean()
                        .param(SimpleBeanCreator.CLASSNAME, clazz.name().toString())
                        .done();
            }
        }));
    }
}

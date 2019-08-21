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

import java.util.UUID;

import javax.faces.model.FacesDataModel;

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
import io.quarkus.myfaces.runtime.QuarkusMyFacesRecorder;
import io.quarkus.myfaces.runtime.producer.FacesDataModelBeanCreator;

public class FacesDataModelBuildStep {

    public static void build(QuarkusMyFacesRecorder recorder,
            BuildProducer<BeanRegistrarBuildItem> beanConfigurators,
            CombinedIndexBuildItem combinedIndex) {

        for (AnnotationInstance ai : combinedIndex.getIndex()
                .getAnnotations(DotName.createSimple(FacesDataModel.class.getName()))) {

            AnnotationValue forClass = ai.value("forClass");
            if (forClass != null) {
                register(beanConfigurators, ai, ai.target().asClass(), forClass.asClass());

                recorder.registerFacesDataModel(ai.target().asClass().name().toString(), forClass.asClass().name().toString());
            }
        }

    }

    private static void register(BuildProducer<BeanRegistrarBuildItem> beanConfigurators,
            AnnotationInstance ai,
            ClassInfo clazz,
            Type forClass) {

        beanConfigurators.produce(new BeanRegistrarBuildItem(new BeanRegistrar() {
            @Override
            public void register(BeanRegistrar.RegistrationContext registrationContext) {
                registrationContext
                        .configure(clazz.name())
                        .qualifiers(ai)
                        .scope(BuiltinScope.DEPENDENT.getInfo())
                        .types(Type.create(clazz.name(), Type.Kind.CLASS))
                        .creator(FacesDataModelBeanCreator.class)
                        .name(UUID.randomUUID().toString().replace("-", ""))
                        .defaultBean()
                        .done();
            }
        }));
    }
}

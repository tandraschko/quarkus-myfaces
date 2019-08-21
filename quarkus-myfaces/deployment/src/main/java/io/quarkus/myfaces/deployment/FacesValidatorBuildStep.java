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

import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;

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
import io.quarkus.myfaces.runtime.producer.SimpleBeanCreator;

public class FacesValidatorBuildStep {

    public static void build(BuildProducer<BeanRegistrarBuildItem> beanConfigurators,
            CombinedIndexBuildItem combinedIndex) {

        for (AnnotationInstance ai : combinedIndex.getIndex()
                .getAnnotations(DotName.createSimple(FacesValidator.class.getName()))) {

            AnnotationValue managed = ai.value("managed");
            if (managed != null && managed.asBoolean()) {
                AnnotationValue value = ai.value("value");
                if (value != null) {
                    if (value.asString() != null && value.asString().length() > 0) {

                        AnnotationValue isDefault = ai.value("isDefault");

                        register(beanConfigurators,
                                ai.target().asClass(),
                                isDefault == null ? null : isDefault.asBoolean(),
                                value.asString());
                    }
                }
            }
        }
    }

    private static void register(BuildProducer<BeanRegistrarBuildItem> beanConfigurators,
            ClassInfo clazz,
            Boolean isDefault,
            String validatorId) {

        if (validatorId == null) {
            validatorId = "";
        }
        if (isDefault == null) {
            isDefault = false;
        }

        List<AnnotationValue> qualifierAttributes = Arrays.asList(
                AnnotationValue.createStringValue("value", validatorId),
                AnnotationValue.createBooleanValue("isDefault", isDefault),
                AnnotationValue.createBooleanValue("managed", true));

        AnnotationInstance qualifier = AnnotationInstance.create(
                DotName.createSimple(FacesValidator.class.getName()),
                null,
                qualifierAttributes);

        beanConfigurators.produce(new BeanRegistrarBuildItem(new BeanRegistrar() {
            @Override
            public void register(BeanRegistrar.RegistrationContext registrationContext) {
                registrationContext
                        .configure(clazz.name())
                        .qualifiers(qualifier)
                        .scope(BuiltinScope.DEPENDENT.getInfo())
                        .types(Type.create(DotName.createSimple(Validator.class.getName()), Type.Kind.CLASS),
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

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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.spi.CreationalContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import io.quarkus.arc.BeanCreator;
import io.quarkus.arc.deployment.BeanRegistrarBuildItem;
import io.quarkus.arc.processor.BeanDeployment;
import io.quarkus.arc.processor.BeanInfo;
import io.quarkus.arc.processor.BeanRegistrar;
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.deployment.annotations.BuildProducer;

public class FacesValidatorExtension {
    public static void register(BuildProducer<BeanRegistrarBuildItem> beanConfigurators,
            ClassInfo clazz,
            String validatorId) {
        System.err.println("FacesValidatorExtension: " + clazz + ", " + validatorId);

        if (validatorId == null) {
            validatorId = "";
        }

        List<AnnotationValue> qualifierAttributes = Arrays.asList(
                AnnotationValue.createStringValue("value", validatorId),
                AnnotationValue.createBooleanValue("managed", true));

        AnnotationInstance qualifier = AnnotationInstance.create(
                DotName.createSimple(FacesValidator.class.getName()),
                null,
                qualifierAttributes);

        beanConfigurators.produce(new BeanRegistrarBuildItem(new BeanRegistrar() {
            @Override
            public void register(BeanRegistrar.RegistrationContext registrationContext) {
                try {
                    System.err.println("#register: ");

                    registrationContext
                            .configure(clazz.name())
                            .qualifiers(qualifier)
                            .scope(BuiltinScope.DEPENDENT.getInfo())
                            .types(Type.create(DotName.createSimple(Validator.class.getName()), Type.Kind.CLASS),
                                    Type.create(clazz.name(), Type.Kind.CLASS))
                            .creator(Fas.class)
                            .done();

                    Field field = registrationContext.getClass().getDeclaredField("this$0");
                    field.setAccessible(true);

                    Object o = field.get(registrationContext);

                    for (BeanInfo bi : ((BeanDeployment) o).getBeans()) {
                        if (bi.getBeanClass().toString().contains("MyValidator")) {
                            System.err.println(bi);
                        }
                    }
                } catch (NoSuchFieldException ex) {
                    Logger.getLogger(FacesValidatorExtension.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(FacesValidatorExtension.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(FacesValidatorExtension.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(FacesValidatorExtension.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }));
    }

    static class Fas implements BeanCreator<Object> {
        @Override
        public Object create(CreationalContext<Object> cc, Map<String, Object> map) {
            System.err.println("#create: " + cc);
            System.err.println(map);

            return null;
        }

    }
}

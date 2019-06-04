/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkus.myfaces.deployment;

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.component.behavior.FacesBehavior;
import javax.faces.convert.FacesConverter;
import javax.faces.render.FacesBehaviorRenderer;
import javax.faces.render.FacesRenderer;
import javax.faces.validator.FacesValidator;
import javax.faces.view.ViewScoped;
import javax.faces.view.facelets.FaceletsResourceResolver;
import javax.faces.webapp.FacesServlet;

import org.apache.myfaces.cdi.FacesScoped;
import org.apache.myfaces.cdi.JsfApplicationArtifactHolder;
import org.apache.myfaces.cdi.JsfArtifactProducer;
import org.apache.myfaces.cdi.config.FacesConfigBeanHolder;
import org.apache.myfaces.cdi.model.FacesDataModelClassBeanHolder;
import org.apache.myfaces.cdi.view.ViewScopeBeanHolder;
import org.apache.myfaces.cdi.view.ViewTransientScoped;
import org.apache.myfaces.config.annotation.CdiAnnotationProviderExtension;
import org.apache.myfaces.config.element.NamedEvent;
import org.apache.myfaces.flow.cdi.FlowScopeBeanHolder;
import org.apache.myfaces.push.cdi.WebsocketApplicationBean;
import org.apache.myfaces.push.cdi.WebsocketChannelTokenBuilderBean;
import org.apache.myfaces.push.cdi.WebsocketSessionBean;
import org.apache.myfaces.push.cdi.WebsocketViewBean;
import org.apache.myfaces.webapp.FaceletsInitilializer;
import org.apache.myfaces.webapp.StartupServletContextListener;
import org.jboss.jandex.DotName;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.arc.deployment.ContextRegistrarBuildItem;
import io.quarkus.arc.processor.ContextRegistrar;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.myfaces.runtime.MyFacesTemplate;
import io.quarkus.myfaces.runtime.QuarkusResourceResolver;
import io.quarkus.myfaces.runtime.QuarkusServletContextListener;
import io.quarkus.myfaces.runtime.scopes.QuarkusFacesScopeContext;
import io.quarkus.myfaces.runtime.scopes.QuarkusViewScopeContext;
import io.quarkus.myfaces.runtime.scopes.QuarkusViewTransientScopeContext;
import io.quarkus.myfaces.runtime.spi.QuarkusInjectionProvider;
import io.quarkus.undertow.deployment.ListenerBuildItem;
import io.quarkus.undertow.deployment.ServletBuildItem;
import io.quarkus.undertow.deployment.ServletInitParamBuildItem;

class MyFacesProcessor {

    private static final Class[] BEAN_CLASSES = {
            JsfApplicationArtifactHolder.class,
            JsfArtifactProducer.class,
            FacesConfigBeanHolder.class,
            FacesDataModelClassBeanHolder.class,
            ViewScopeBeanHolder.class,
            WebsocketChannelTokenBuilderBean.class,
            WebsocketSessionBean.class,
            WebsocketViewBean.class,
            WebsocketApplicationBean.class,
            FlowScopeBeanHolder.class,
            CdiAnnotationProviderExtension.class
    };

    private static final String[] BEAN_DEFINING_ANNOTATION_CLASSES = {
            FacesComponent.class.getName(),
            FacesBehavior.class.getName(),
            FacesConverter.class.getName(),
            FacesValidator.class.getName(),
            FacesRenderer.class.getName(),
            NamedEvent.class.getName(),
            FacesBehaviorRenderer.class.getName(),
            FaceletsResourceResolver.class.getName()
    };

    @BuildStep
    void buildFeature(BuildProducer<FeatureBuildItem> feature) throws IOException {
        feature.produce(new FeatureBuildItem("myfaces"));
    }

    @BuildStep
    void buildServlet(BuildProducer<FeatureBuildItem> feature,
            BuildProducer<ServletBuildItem> servlet,
            BuildProducer<ListenerBuildItem> listener) throws IOException {

        servlet.produce(ServletBuildItem.builder("Faces Servlet", FacesServlet.class.getName())
                .setLoadOnStartup(1)
                .addMapping("*.xhtml")
                .build());

        listener.produce(new ListenerBuildItem(QuarkusServletContextListener.class.getName()));
        listener.produce(new ListenerBuildItem(StartupServletContextListener.class.getName()));
    }

    @BuildStep
    void buildCdiBeans(BuildProducer<FeatureBuildItem> feature,
            BuildProducer<ServletBuildItem> servlet,
            BuildProducer<ListenerBuildItem> listener,
            BuildProducer<AdditionalBeanBuildItem> additionalBean,
            BuildProducer<BeanDefiningAnnotationBuildItem> beanDefiningAnnotation,
            BuildProducer<ContextRegistrarBuildItem> contextRegistrar) throws IOException {

        for (Class<?> clazz : BEAN_CLASSES) {
            additionalBean.produce(AdditionalBeanBuildItem.unremovableOf(clazz));
        }

        for (String clazz : BEAN_DEFINING_ANNOTATION_CLASSES) {
            beanDefiningAnnotation.produce(new BeanDefiningAnnotationBuildItem(DotName.createSimple(clazz)));
        }

    }

    @BuildStep
    void buildCdiScopes(BuildProducer<ContextRegistrarBuildItem> contextRegistrar) throws IOException {

        contextRegistrar.produce(new ContextRegistrarBuildItem(new ContextRegistrar() {
            @Override
            public void register(ContextRegistrar.RegistrationContext registrationContext) {
                registrationContext.configure(ViewScoped.class).normal().contextClass(QuarkusViewScopeContext.class).done();
                registrationContext.configure(FacesScoped.class).normal().contextClass(QuarkusFacesScopeContext.class).done();
                registrationContext.configure(ViewTransientScoped.class).normal()
                        .contextClass(QuarkusViewTransientScopeContext.class).done();
            }
        }));
    }

    @BuildStep
    void buildInitParams(BuildProducer<ServletInitParamBuildItem> initParam) throws IOException {

        initParam.produce(new ServletInitParamBuildItem(
                "org.apache.myfaces.spi.InjectionProvider", QuarkusInjectionProvider.class.getName()));
        initParam.produce(new ServletInitParamBuildItem(
                "org.apache.myfaces.FACES_INITIALIZER", FaceletsInitilializer.class.getName()));
        initParam.produce(new ServletInitParamBuildItem(
                "org.apache.myfaces.SUPPORT_JSP", "false"));
        initParam.produce(new ServletInitParamBuildItem(
                "org.apache.myfaces.CDI_PASSIVATION_SUPPORTED", "false"));

        initParam.produce(new ServletInitParamBuildItem(
                "javax.faces.FACELETS_RESOURCE_RESOLVER", QuarkusResourceResolver.class.getName()));
    }

    @BuildStep
    void buildRecommendedInitParams(BuildProducer<ServletInitParamBuildItem> initParam) throws IOException {

        initParam.produce(new ServletInitParamBuildItem(
                "org.apache.myfaces.LOG_WEB_CONTEXT_PARAMS", "false"));
        initParam.produce(new ServletInitParamBuildItem(
                "javax.faces.STATE_SAVING_METHOD", "server"));
        initParam.produce(new ServletInitParamBuildItem(
                "javax.faces.SERIALIZE_SERVER_STATE", "false"));

        // perf
        initParam.produce(new ServletInitParamBuildItem(
                "org.apache.myfaces.CHECK_ID_PRODUCTION_MODE", "false"));
        initParam.produce(new ServletInitParamBuildItem(
                "org.apache.myfaces.EARLY_FLUSH_ENABLED", "true"));
        initParam.produce(new ServletInitParamBuildItem(
                "org.apache.myfaces.CACHE_EL_EXPRESSIONS", "alwaysRecompile"));
        initParam.produce(new ServletInitParamBuildItem(
                "org.apache.myfaces.COMPRESS_STATE_IN_SESSION", "false"));

        initParam.produce(new ServletInitParamBuildItem(
                "org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION", "15"));
        initParam.produce(new ServletInitParamBuildItem(
                "org.apache.myfaces.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION", "3"));

        // primefaces perf
        initParam.produce(new ServletInitParamBuildItem(
                "primefaces.SUBMIT", "partial"));
        initParam.produce(new ServletInitParamBuildItem(
                "primefaces.MOVE_SCRIPTS_TO_BOTTOM", "true"));
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void buildAnnotationProviderIntegration(MyFacesTemplate template, CombinedIndexBuildItem combinedIndex) throws IOException {

        for (String clazz : BEAN_DEFINING_ANNOTATION_CLASSES) {
            combinedIndex.getIndex()
                    .getAnnotations(DotName.createSimple(clazz))
                    .stream()
                    .forEach(annotation -> template.registerAnnotatedClass(annotation.name().toString(),
                            annotation.target().asClass().name().toString()));
        }
    }
}

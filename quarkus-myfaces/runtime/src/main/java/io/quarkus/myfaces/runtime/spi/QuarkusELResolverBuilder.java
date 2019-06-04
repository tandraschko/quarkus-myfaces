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
package io.quarkus.myfaces.runtime.spi;

import java.util.ArrayList;
import java.util.List;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELResolver;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.StaticFieldELResolver;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.FacesContext;

import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.el.ELResolverBuilder;
import org.apache.myfaces.el.resolver.CompositeComponentELResolver;
import org.apache.myfaces.el.resolver.FacesCompositeELResolver;
import org.apache.myfaces.el.resolver.FlashELResolver;
import org.apache.myfaces.el.resolver.ImportConstantsELResolver;
import org.apache.myfaces.el.resolver.ImportHandlerResolver;
import org.apache.myfaces.el.resolver.ResourceBundleResolver;
import org.apache.myfaces.el.resolver.ResourceResolver;
import org.apache.myfaces.el.resolver.ScopedAttributeResolver;
import org.apache.myfaces.el.resolver.implicitobject.ImplicitObjectResolver;

/**
 * Custom {@link ELResolverBuilder} which only works with EL3.x+
 * and replaces the {@link BeanManager#getELResolver()} with our own {@link QuarkusCdiELResolver}
 */
public class QuarkusELResolverBuilder extends ELResolverBuilder {
    public QuarkusELResolverBuilder(RuntimeConfig runtimeConfig, MyfacesConfig myfacesConfig) {
        super(runtimeConfig, myfacesConfig);
    }

    @Override
    public void build(FacesContext facesContext, CompositeELResolver compositeElResolver) {
        MyfacesConfig config = MyfacesConfig.getCurrentInstance(FacesContext.getCurrentInstance());

        // add the ELResolvers to a List first to be able to sort them
        List<ELResolver> list = new ArrayList<>();

        // Add CDI ELResolver for JSF 2.3
        list.add(ImplicitObjectResolver.makeResolverForFacesCDI());
        list.add(new QuarkusCdiELResolver());

        list.add(new CompositeComponentELResolver());

        addFromRuntimeConfig(list);

        //Flash object is instanceof Map, so it is necessary to resolve
        //before MapELResolver. Better to put this one before
        list.add(new FlashELResolver());
        list.add(new ResourceResolver());
        list.add(new ResourceBundleELResolver());
        list.add(new ResourceBundleResolver());
        list.add(new ImportConstantsELResolver());

        list.add(runtimeConfig.getExpressionFactory().getStreamELResolver());
        list.add(new StaticFieldELResolver());

        list.add(new MapELResolver());
        list.add(new ListELResolver());
        list.add(new ArrayELResolver());
        list.add(new BeanELResolver());

        // give the user a chance to sort the resolvers
        sortELResolvers(list, FacesCompositeELResolver.Scope.Faces);

        // give the user a chance to filter the resolvers
        Iterable<ELResolver> filteredELResolvers = filterELResolvers(list, FacesCompositeELResolver.Scope.Faces);

        // add the resolvers from the list to the CompositeELResolver
        for (ELResolver resolver : filteredELResolvers) {
            compositeElResolver.add(resolver);
        }

        // Only add this resolver if the user wants to use the EL ImportHandler
        if (config.isSupportEL3ImportHandler()) {
            compositeElResolver.add(new ImportHandlerResolver());
        }

        // the ScopedAttributeResolver has to be the last one in every
        // case, because it always sets propertyResolved to true (per the spec)
        compositeElResolver.add(new ScopedAttributeResolver());
    }

}

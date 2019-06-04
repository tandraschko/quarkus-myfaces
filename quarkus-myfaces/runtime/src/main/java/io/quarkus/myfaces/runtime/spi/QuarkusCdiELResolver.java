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

import java.beans.FeatureDescriptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.faces.FacesException;

/**
 * Custom ELResolver for CDI as BeanManager#getELResolver is not supported on Quarkus.
 * TODO: It probably needs some caching
 */
public class QuarkusCdiELResolver extends ELResolver {
    private BeanManager beanManager;
    private Map<String, Object> cachedProxies;

    public QuarkusCdiELResolver() {
        beanManager = CDI.current().getBeanManager();
        cachedProxies = new HashMap<>();
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext arg0, Object arg1) {
        return null;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext arg0, Object arg1) {
        return null;
    }

    @Override
    public Class<?> getType(ELContext arg0, Object arg1, Object arg2) throws ELException {
        return null;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) throws ELException {
        //we only check root beans
        if (base != null) {
            return null;
        }

        String beanName = (String) property;

        // try cache lookup first
        Object contextualInstance = cachedProxies.get(beanName);
        if (contextualInstance != null) {
            context.setPropertyResolved(true);
            return contextualInstance;
        }

        Set<Bean<?>> beans = beanManager.getBeans(beanName);
        if (beans != null && !beans.isEmpty()) {
            Bean<?> bean = beanManager.resolve(beans);

            if (bean.getScope().equals(Dependent.class)) {
                throw new FacesException("@Dependent on beans used in EL are currently not supported! "
                        + " Class: " + bean.getBeanClass().toString());
            }

            CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
            contextualInstance = beanManager.getReference(bean, Object.class, creationalContext);
            if (contextualInstance != null) {
                context.setPropertyResolved(true);

                cachedProxies.put(beanName, contextualInstance);
            }

            return contextualInstance;
        }

        return null;
    }

    @Override
    public boolean isReadOnly(ELContext arg0, Object arg1, Object arg2) throws ELException {
        return false;
    }

    @Override
    public void setValue(ELContext arg0, Object arg1, Object arg2, Object arg3) throws ELException {

    }
}

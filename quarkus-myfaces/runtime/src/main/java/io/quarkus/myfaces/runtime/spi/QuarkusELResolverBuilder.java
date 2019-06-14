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

import javax.el.ELResolver;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.el.ELResolverBuilder;
import org.apache.myfaces.el.ELResolverBuilderForFaces;

/**
 * Custom {@link ELResolverBuilder} which only works with EL3.x+
 * and replaces the {@link BeanManager#getELResolver()} with our own {@link QuarkusCdiELResolver}
 */
public class QuarkusELResolverBuilder extends ELResolverBuilderForFaces {

    public QuarkusELResolverBuilder(RuntimeConfig runtimeConfig, MyfacesConfig myfacesConfig) {
        super(runtimeConfig, myfacesConfig);
    }

    @Override
    protected ELResolver getCDIELResolver() {
        return new QuarkusCdiELResolver();
    }
}

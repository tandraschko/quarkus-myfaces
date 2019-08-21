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
package io.quarkus.myfaces.runtime.producer;

import java.util.Map;

import javax.enterprise.context.spi.CreationalContext;

import org.apache.myfaces.util.lang.ClassUtils;

import io.quarkus.arc.BeanCreator;

public class SimpleBeanCreator implements BeanCreator<Object> {

    public static final String CLASSNAME = "clazz";

    @Override
    public Object create(CreationalContext<Object> cc, Map<String, Object> map) {
        return ClassUtils.newInstance((String) map.get(CLASSNAME));
    }

}

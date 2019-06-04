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
package io.quarkus.myfaces.runtime.myfaces;

import java.beans.PropertyDescriptor;
import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MethodHandlePropertyAccessor {

    private static final Map<String, Map<String, Function>> GETTER_CACHE = new ConcurrentHashMap<String, Map<String, Function>>();
    private static final boolean SUPPORTED;
    
    static {
        Method privateLookupIn = null;
        try {
            privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
        } catch (Exception e) {
        }
        SUPPORTED = privateLookupIn != null;
    }

    private MethodHandlePropertyAccessor() {
    }

    public static <T> T getFieldValue(Object target, String fieldName) {
        Class<?> targetClass = target.getClass();
        Map<String, Function> cache = GETTER_CACHE.computeIfAbsent(targetClass.getName(), k -> new ConcurrentHashMap<>());
        Function func = cache.computeIfAbsent(fieldName, k -> createGetter(targetClass, fieldName));

        return (T) func.apply(target);
    }

    private static Function createGetter(Class<?> target, String fieldName) {

        try {
            PropertyDescriptor pd = new PropertyDescriptor(fieldName, target);
            Method getter = pd.getReadMethod();

            //java9+
            Method m = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
            MethodHandles.Lookup lookup = (MethodHandles.Lookup) m.invoke(null, target, MethodHandles.lookup());

            MethodHandle methodHandle = lookup.findVirtual(target, getter.getName(),
                    MethodType.methodType(getter.getReturnType()));

            CallSite site = LambdaMetafactory.metafactory(lookup,
                    "apply",
                    MethodType.methodType(Function.class),
                    MethodType.methodType(Object.class, Object.class),
                    methodHandle,
                    MethodType.methodType(getter.getReturnType(), target));
            return (Function) site.getTarget().invokeExact();

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}

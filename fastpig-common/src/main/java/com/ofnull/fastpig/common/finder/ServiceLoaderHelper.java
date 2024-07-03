package com.ofnull.fastpig.common.finder;

import java.lang.reflect.Constructor;

/**
 * @author ofnull
 * @date 2024/6/6 10:39
 */
public class ServiceLoaderHelper {

    public static <T> T loadServices(Class<T> clazz, String name) {
        try {
            ExtensionLoader<T> extensionLoader = ExtensionDirector.findExtensionLoader(clazz);
            Class<?> result = extensionLoader.findExtend(name);
            T instantiate = instantiate(result);
            return instantiate;
        } catch (Exception e) {
            throw new RuntimeException("serviceLoader fail name: " + name + " clazz:" + clazz, e);
        }
    }

    private static <T> T instantiate(Class<?> type) throws ReflectiveOperationException {
        Constructor<?> defaultConstructor = type.getConstructor();
        T instance = (T) defaultConstructor.newInstance();
        return instance;
    }

}

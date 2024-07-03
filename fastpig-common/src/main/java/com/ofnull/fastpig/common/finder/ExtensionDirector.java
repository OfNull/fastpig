package com.ofnull.fastpig.common.finder;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ofnull
 * @date 2022/2/9 17:27
 */
public class ExtensionDirector {
    private static Map<Class<?>, ExtensionLoader> extendLoaderMap = new ConcurrentHashMap<>(6);

    public static <T> ExtensionLoader<T> findExtensionLoader(Class<T> type) {
        if (Objects.isNull(type)) {
            //
            throw new IllegalArgumentException("type is null!");
        }
        if (!(type.isInterface() || Modifier.isAbstract(type.getModifiers()))) {
            //
            throw new IllegalArgumentException("type not an interface|abstract ! ClassName: " + type.getName());
        }
        ExtensionLoader<T> extensionLoader = extendLoaderMap.get(type);
        if (Objects.isNull(extensionLoader)) {
            extensionLoader = createExtensionLoader(type);
            extendLoaderMap.putIfAbsent(type, extensionLoader);
        }
        return extensionLoader;
    }

    private static <T> ExtensionLoader<T> createExtensionLoader(Class<T> clazz) {
        return new ExtensionLoader<T>(clazz);
    }
}

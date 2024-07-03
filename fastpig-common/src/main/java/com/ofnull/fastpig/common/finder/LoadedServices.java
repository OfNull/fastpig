package com.ofnull.fastpig.common.finder;

import com.ofnull.fastpig.common.compiler.SourceCompiler;
import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.finder.IServiceHolder;
import com.ofnull.fastpig.spi.metainfo.SourceClassDefinition;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author ofnull
 * @date 2024/6/6 10:23
 */
public class LoadedServices<T> implements Closeable, IServiceHolder<T> {
    private static final Logger LOG = LoggerFactory.getLogger(LoadedServices.class);
    private Map<String, Class<T>> classes = new HashMap<>();
    private Map<String, T> services = new HashMap<>();
    private SourceCompiler sourceCompiler;

    public static <T> LoadedServices<T> of(List<SourceClassDefinition> definitions) throws ClassNotFoundException {
        return of(null, definitions);
    }

    public static <T> LoadedServices<T> of(ClassLoader classLoader, List<SourceClassDefinition> definitions) throws ClassNotFoundException {
        Map<String, Class<T>> classes = new HashMap<>();
        SourceCompiler sourceCompiler = new SourceCompiler(classLoader);
        if (CollectionUtils.isNotEmpty(definitions)) {
            for (SourceClassDefinition definition : definitions) {
                Class<?> clazz = sourceCompiler.compiler(definition.getDefinition());
                classes.put(definition.getName(), (Class<T>) clazz);
            }
        }
        LoadedServices<T> loadedServices = new LoadedServices<T>(classes, sourceCompiler);
        return loadedServices;
    }

    public static <T> LoadedServices<T> of(Class<T> clazz) throws IOException, URISyntaxException {
        ExtensionLoader<T> extensionLoader = ExtensionDirector.findExtensionLoader(clazz);
        List<Class<?>> classList = extensionLoader.findExtendAll();
        Map<String, Class<T>> classes = new HashMap<>();
        for (Class<?> aClass : classList) {
            PigType pigType = aClass.getAnnotation(PigType.class);
            if (pigType == null) {
                throw new IllegalArgumentException("PigType annotation not found in class " + aClass.getName());
            }
            classes.put(pigType.value(), (Class<T>) aClass);
        }
        LoadedServices<T> loadedServices = new LoadedServices<T>(classes);
        return loadedServices;
    }


    private LoadedServices() {
    }

    private LoadedServices(Map<String, Class<T>> classes, SourceCompiler sourceCompiler) {
        this.classes = classes;
        this.sourceCompiler = sourceCompiler;
    }

    private LoadedServices(Map<String, Class<T>> classes) {
        this(classes, null);
    }

    @Override
    public T findService(String name, Class<T> type) throws Exception {
        T service = services.get(name);
        if (Objects.nonNull(service)) {
            return service;
        }
        return createNewService(name, type);
    }

    @Override
    public T createNewService(String name, Class<T> type) throws Exception {
        Class<T> clazz = classes.get(name);
        if (clazz == null) {
            throw new IllegalArgumentException("No such impl, name " + name + " type " + type.getName());
        }
        T instance = clazz.newInstance();
        if (services.containsKey(name)) {
            LOG.warn("Service already exists, execute overwrite. name:{}, type:{}", name, type.getName());
        }
        services.put(name, instance);
        return instance;
    }

    @Override
    public void close() throws IOException {
        if (MapUtils.isNotEmpty(services)) {
            services.forEach((k, v) -> {
                try {
                    if (v instanceof Closeable) {
                        ((Closeable) v).close();
                    }
                } catch (Exception e) {
                    LOG.warn("Closeable Resource Exception, name:{}, type:{}", k, v.getClass().getName());
                }
            });
        }
        if (Objects.nonNull(sourceCompiler)) {
            sourceCompiler.close();
        }
    }
}

package com.ofnull.fastpig.common.compiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.beans.Introspector;
import java.io.IOException;
import java.util.*;

/**
 * @author ofnull
 * @date 2024/4/15 17:26
 */
public class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    public final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String[] superLocationNames = new String[]{StandardLocation.PLATFORM_CLASS_PATH.name(), "SYSTEM_MODULES"};
    private Map<String, JavaClassFileObject> classFileMap = new HashMap<>();
    private ClassLoader classLoader;
    private PackageInternalsFinder finder;

    public MemoryFileManager(JavaFileManager fileManager, ClassLoader classLoader) {
        super(fileManager);
        this.classLoader = new MemoryClassLoader(classLoader, classFileMap);
        this.finder = new PackageInternalsFinder(classLoader);
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return classLoader;
    }

    /**
     * 获取class输出位置
     *
     * @return
     */
    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        JavaClassFileObject classFileObject = new JavaClassFileObject(className);
        classFileMap.put(className, classFileObject);
        return classFileObject;
    }

    /**
     * 根据location类型 加载对应的JavaFileObject
     *
     * @param location
     * @param packageName
     * @param kinds
     * @param recurse
     * @return
     * @throws IOException
     */
    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        if (location instanceof StandardLocation) {
            String locationName = ((StandardLocation) location).name();
            for (String name : superLocationNames) {
                if (name.equals(locationName)) {
                    super.list(location, packageName, kinds, recurse);
                }
            }
        }
        if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
            return new IterableJoin<>(super.list(location, packageName, kinds, recurse), finder.find(packageName));
        }

        return super.list(location, packageName, kinds, recurse);
    }

    /**
     * 查找class二进制名称
     *
     * @param location
     * @param file
     * @return
     */
    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file instanceof LibJavaFileObject) {
            return ((LibJavaFileObject) file).binaryName();
        }
        return super.inferBinaryName(location, file);
    }

    static class IterableJoin<T> implements Iterable<T> {
        private final Iterable<T> first, next;

        public IterableJoin(Iterable<T> first, Iterable<T> next) {
            this.first = first;
            this.next = next;
        }

        @Override
        public Iterator<T> iterator() {
            return new IteratorJoin<T>(first.iterator(), next.iterator());
        }
    }

    static class IteratorJoin<T> implements Iterator<T> {
        private final Iterator<T> first, next;

        public IteratorJoin(Iterator<T> first, Iterator<T> next) {
            this.first = first;
            this.next = next;
        }

        @Override
        public boolean hasNext() {
            return first.hasNext() || next.hasNext();
        }

        @Override
        public T next() {
            if (first.hasNext()) {
                return first.next();
            }
            return next.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (this.classLoader != getClass().getClassLoader()) {
            Introspector.flushCaches();
            ResourceBundle.clearCache(classLoader);
        }
    }
}

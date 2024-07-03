package com.ofnull.fastpig.common.compiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 自定义类加载器
 *
 * @author ofnull
 * @date 2024/4/15 17:30
 */
public class MemoryClassLoader extends ClassLoader {
    public final Logger logger = LoggerFactory.getLogger(getClass());
    private Map<String, JavaClassFileObject> classFileMap = new HashMap<>();

    public MemoryClassLoader(ClassLoader parent, Map<String, JavaClassFileObject> classFileMap) {
        super(Optional.ofNullable(parent).orElse(Thread.currentThread().getContextClassLoader()));
        this.classFileMap = Optional.ofNullable(classFileMap).orElse(new HashMap<>());
    }


    /**
     * 重写类加载 优先从内存加载class 找不到再交给父类加载
     *
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        JavaClassFileObject classFileObject = classFileMap.get(className);
        if (classFileObject != null) {
            byte[] byteCode = classFileObject.getByteCode();
            return defineClass(className, byteCode, 0, byteCode.length);
        }
        return super.loadClass(className);
    }

    /**
     * 添加 JavaClassFileObject
     *
     * @param className
     * @param byteClass
     */
    public void addClassFile(String className, JavaClassFileObject byteClass) {
        if (classFileMap.containsKey(className)) {
            logger.warn("class {} already javaClassFileObject,  overwrite it.", className);
        }
        classFileMap.put(className, byteClass);
    }

    /**
     * 添加 JavaClassFileObject
     */
    public void addClassFile(Map<String, JavaClassFileObject> classFileMap) {
        if (classFileMap != null && !classFileMap.isEmpty()) {
            for (Map.Entry<String, JavaClassFileObject> entry : classFileMap.entrySet()) {
                addClassFile(entry.getKey(), entry.getValue());
            }
        }
    }
}

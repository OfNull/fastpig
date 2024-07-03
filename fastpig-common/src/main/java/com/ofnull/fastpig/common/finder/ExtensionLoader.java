package com.ofnull.fastpig.common.finder;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ofnull
 * @date 2022/2/9 16:10
 */
public class ExtensionLoader<T> {
    private static final Logger LOG = LoggerFactory.getLogger(ExtensionLoader.class);
    private static final String PREFIX = "META-INFO/fastpig/internal/";

    private Map<String, Class<?>> extensionClasses = new ConcurrentHashMap<>(6);

    private Class<T> type;

    public ExtensionLoader(Class<T> type) {
        this.type = type;
    }

    public Class<?> findExtend(String name) throws IOException, URISyntaxException {
        if (StringUtils.isEmpty(name)) {
            LOG.warn("find name Empty. type:{}", type.getName());
            return null;
        }
        Class<?> clazz = extensionClasses.get(name);
        if (clazz == null) {
            readParseMetaInfo();
        }
        clazz = extensionClasses.get(name);

        return clazz;
    }

    public List<Class<?>> findExtendAll() throws IOException, URISyntaxException {
        if (MapUtils.isNotEmpty(extensionClasses)) {
            return new ArrayList<>(extensionClasses.values());
        }
        readParseMetaInfo();
        return new ArrayList<>(extensionClasses.values());
    }

    private void readParseMetaInfo() throws IOException, URISyntaxException {
        String fullName = PREFIX + type.getName();
        Enumeration<URL> resources = ClassLoader.getSystemResources(fullName);
        if (Objects.nonNull(resources)) {
            while (resources.hasMoreElements()) {
                readParseMetaInfo(resources.nextElement().toURI().toURL());
            }
        }
    }

    private void readParseMetaInfo(URL url) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));) {
            String line;
            String clazz;
            while ((line = reader.readLine()) != null) {
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        String name = null;
                        int i = line.indexOf('=');
                        if (i > 0) {
                            name = line.substring(0, i).trim();
                            clazz = line.substring(i + 1).trim();
                        } else {
                            clazz = line;
                        }
                        if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(clazz)) {
                            extensionClasses.put(name, Class.forName(clazz));
                        }
                    } catch (Throwable t) {
                        //todo
                        throw t;
                    }
                }
            }

        } catch (Exception e) {
            //
        }
    }

}

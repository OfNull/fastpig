package com.ofnull.fastpig.common.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;

/**
 * javaSource String文件对象
 *
 * @author ofnull
 * @date 2024/4/15 17:06
 */
public class JavaSourceFileObject extends SimpleJavaFileObject {
    private static final char PKG_SEPARATOR = '.';
    private static final char DIR_SEPARATOR = '/';
    private String sourceCode;

    /**
     * @param name       className 包名+类名
     * @param sourceCode java源码
     */
    public JavaSourceFileObject(String name, String sourceCode) {
        super(URI.create("string:///" + name.replace(PKG_SEPARATOR, DIR_SEPARATOR) + Kind.SOURCE.extension), Kind.SOURCE);
        this.sourceCode = sourceCode;
    }

    /**
     * 返回java源码
     *
     * @param ignoreEncodingErrors
     * @return
     * @throws IOException
     */
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return sourceCode;
    }
}

package com.ofnull.fastpig.common.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.*;
import java.net.URI;

/**
 * Class java文件对象描述
 *
 * @author ofnull
 * @date 2024/4/15 17:13
 */
public class JavaClassFileObject extends SimpleJavaFileObject {
    private static final char PKG_SEPARATOR = '.';
    private static final char DIR_SEPARATOR = '/';
    private ByteArrayOutputStream byteArrayOutputStream;

    /**
     * @param className className 包名+类名
     */
    public JavaClassFileObject(String className) {
        super(URI.create("byte:///" + className.replace(PKG_SEPARATOR, DIR_SEPARATOR) + Kind.CLASS.extension), Kind.CLASS);
    }


    /**
     * 打开一个输入流
     *
     * @return
     * @throws IOException
     */
    @Override
    public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(getByteCode());
    }

    /**
     * 打开一个输出流
     *
     * @return
     * @throws IOException
     */
    @Override
    public OutputStream openOutputStream() throws IOException {
        if (byteArrayOutputStream == null) {
            byteArrayOutputStream = new ByteArrayOutputStream();
        }
        return byteArrayOutputStream;
    }

    /**
     * 从输出流获取获取class字节数组
     *
     * @return
     */
    public byte[] getByteCode() {
        return byteArrayOutputStream.toByteArray();
    }
}

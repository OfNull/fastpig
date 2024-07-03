package com.ofnull.fastpig.common.compiler;


import javax.tools.*;
import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.MULTILINE;

/**
 * @author ofnull
 * @date 2024/4/15 18:28
 */
public class SourceCompiler implements Closeable {
    private static final Pattern classNamePattern = Pattern.compile("package\\s+(?<package>\\S+)\\s*;.*?(class|interface)\\s+(?<class>\\S+)\\s+", MULTILINE | DOTALL);

    public static final JavaCompiler SYSTEM_COMPILER = ToolProvider.getSystemJavaCompiler();
    private List<String> options;
    private MemoryFileManager fileManager;
    private DiagnosticCollector diagnostics;

    {
        this.options = Arrays.asList("-g", "-nowarn");
    }

    public SourceCompiler(ClassLoader classLoader) {
        classLoader = Optional.ofNullable(classLoader).orElse(Thread.currentThread().getContextClassLoader());
        StandardJavaFileManager standardFileManager = SYSTEM_COMPILER.getStandardFileManager(null, null, null);
        fileManager = new MemoryFileManager(standardFileManager, classLoader);
        diagnostics = DiagnosticUtil.genDiagnosticCollector();
    }

    private Iterable<JavaFileObject> getCompilationUnits(String... sourceCodes) {
        List<JavaFileObject> compilationUnits = new ArrayList<>();
        for (String sourceCode : sourceCodes) {
            final String className = matcherClassName(sourceCode);
            JavaSourceFileObject sourceFileObject = new JavaSourceFileObject(className, sourceCode);
            compilationUnits.add(sourceFileObject);

        }
        return compilationUnits;
    }

    private Iterable<JavaFileObject> getCompilationUnits(Map<String, String> sourceCodeMap) {
        List<JavaFileObject> compilationUnits = new ArrayList<>();
        for (Map.Entry<String, String> entry : sourceCodeMap.entrySet()) {
            String className = entry.getKey();
            String sourceCode = entry.getValue();
            JavaSourceFileObject sourceFileObject = new JavaSourceFileObject(className, sourceCode);
            compilationUnits.add(sourceFileObject);
        }
        return compilationUnits;
    }

    public JavaCompiler.CompilationTask getTask(
            JavaFileManager fileManager,
            DiagnosticListener<? super JavaFileObject> diagnosticListener,
            Iterable<String> options,
            Iterable<? extends JavaFileObject> compilationUnits) {
        return SYSTEM_COMPILER.getTask(null, fileManager, diagnosticListener, options, null, compilationUnits);
    }

    private Map<String, Class<?>> compilerAndClass(Map<String, String> sourceCodeMap) throws ClassNotFoundException {
        if (sourceCodeMap == null || sourceCodeMap.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        Iterable<JavaFileObject> compilationUnits = getCompilationUnits(sourceCodeMap);
        JavaCompiler.CompilationTask task = getTask(fileManager, diagnostics, options, compilationUnits);
        Map<String, Class<?>> classMap = new HashMap<>();
        if (task.call()) {
            ClassLoader classLoader = fileManager.getClassLoader(StandardLocation.CLASS_OUTPUT);
            for (String className : sourceCodeMap.keySet()) {
                Class<?> aClass = classLoader.loadClass(className);
                classMap.put(className, aClass);
            }
        }
        return classMap;
    }

    public Class<?> compiler(String sourceCode) throws ClassNotFoundException {
        final String className = matcherClassName(sourceCode);
        Iterable<JavaFileObject> compilationUnits = getCompilationUnits(sourceCode);
        Boolean isSuccess = SYSTEM_COMPILER.getTask(null, fileManager, diagnostics, options, null, compilationUnits).call();
        if (isSuccess) {
            ClassLoader classLoader = fileManager.getClassLoader(StandardLocation.CLASS_OUTPUT);
            return classLoader.loadClass(className);
        }
        throw new RuntimeException(DiagnosticUtil.getMessages(diagnostics));
    }


    protected String matcherClassName(String sourceCode) {
        Matcher matcher = classNamePattern.matcher(sourceCode);
        if (!matcher.find()) {
            throw new RuntimeException("Class name not found");
        }
        return matcher.group("package") + "." + matcher.group("class");
    }

    @Override
    public void close() throws IOException {
        if (fileManager != null) {
            fileManager.close();
        }
    }
}

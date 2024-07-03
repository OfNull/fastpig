package com.ofnull.fastpig.common.compiler;

import javax.tools.DiagnosticCollector;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 诊断器工具类
 *
 * @author ofnull
 * @date 2024/4/15 18:21
 */
public class DiagnosticUtil {

    /**
     * 生成集合诊断器
     *
     * @return
     */
    public static DiagnosticCollector genDiagnosticCollector() {
        return new DiagnosticCollector<>();
    }

    /**
     * 编译诊断收集信息
     *
     * @param collector
     * @return
     */
    public static String getMessages(DiagnosticCollector<?> collector) {
        final List<?> diagnostics = collector.getDiagnostics();
        return diagnostics.stream().map(String::valueOf)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}

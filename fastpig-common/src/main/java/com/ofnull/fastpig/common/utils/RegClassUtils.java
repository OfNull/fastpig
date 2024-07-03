package com.ofnull.fastpig.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ofnull
 * @date 2022/9/9 16:19
 */
public class RegClassUtils {
    private static final Pattern CLASS_PATTERN = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s*");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s([a-zA-Z_][a-zA-Z0-9_]*)+([.][a-zA-Z_][a-zA-Z0-9_]*)+;");

    public static String getPackage(String sourceCode) {
        Matcher matcher = PACKAGE_PATTERN.matcher(sourceCode);
        if (matcher.find()) {
            return matcher.group(0).replace(";", "").split("\\s")[1];
        }
        return "";
    }

    public static String getClassName(String sourceCode) {
        Matcher matcher = CLASS_PATTERN.matcher(sourceCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("No class name found in: \n[" + sourceCode + "]");
    }
}
